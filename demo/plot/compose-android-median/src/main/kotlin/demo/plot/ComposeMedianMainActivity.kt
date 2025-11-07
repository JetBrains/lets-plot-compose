/*
 * Copyright (c) 2023 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package demo.plot

import android.Manifest
import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import demo.plot.ui.DemoDropdownMenu
import demo.plot.ui.DemoRadioGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.letsPlot.Figure
import org.jetbrains.letsPlot.compose.MissingStoragePermissionException
import org.jetbrains.letsPlot.compose.PlotPanel
import org.jetbrains.letsPlot.compose.PlotPanelRaw
import org.jetbrains.letsPlot.compose.ggsave
import org.jetbrains.letsPlot.core.util.PlotExportCommon.SizeUnit
import plotSpec.*

// Data class to hold the parameters for a save operation that was deferred due to a missing permission
private data class PendingSave(
    val figure: Figure,
    val filename: String,
    val scale: Double?,
    val dpi: Int?,
    val w: Double?,
    val h: Double?,
    val unit: SizeUnit
)

class ComposeMedianMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val figures = listOf(
                "Density" to DensitySpec().createFigure(),
                "gggrid" to PlotGridSpec().createFigure(),
                "Raster" to RasterSpec().createFigure(),
                "Bar" to BarPlotSpec().createFigure(),
                "Violin" to ViolinSpec().createFigure(),
                "Markdown" to MarkdownSpec().mpg(),
                "BackendError" to IllegalArgumentSpec().createFigure(),
                "FrontendError" to FrontendExceptionSpec().createRawSpec(),
            )

            val preserveAspectRatio = rememberSaveable { mutableStateOf(true) }
            val figureIndex = rememberSaveable { mutableStateOf(3) }
            val coroutineScope = rememberCoroutineScope()

            val showSaveDialog = remember { mutableStateOf(false) }
            val savedImagePath = remember { mutableStateOf<String?>(null) }
            // State to hold a save operation while we ask for permission
            val pendingSave = remember { mutableStateOf<PendingSave?>(null) }

            // Dialog input fields state
            val filenameState = remember { mutableStateOf("") }
            val scaleState = remember { mutableStateOf("1.0") }
            val dpiState = remember { mutableStateOf("") }
            val widthState = remember { mutableStateOf("") }
            val heightState = remember { mutableStateOf("") }
            val selectedUnit = remember { mutableStateOf(SizeUnit.PX) }

            // Helper function to perform the save, avoiding code duplication
            val performSave: (PendingSave) -> Unit = { saveParams ->
                try {
                    val path = ggsave(
                        plot = saveParams.figure,
                        filename = saveParams.filename,
                        scale = saveParams.scale,
                        dpi = saveParams.dpi,
                        w = saveParams.w,
                        h = saveParams.h,
                        unit = saveParams.unit,
                        context = this
                    )

                    scanFileToShowInGallery(this, path)
                    println("Plot saved to: $path")
                    Toast.makeText(this, "Saved to $path", Toast.LENGTH_LONG).show()
                    savedImagePath.value = path

                } catch (e: Exception) {
                    // Handle other potential errors from ggsave (e.g., bad filename)
                    e.printStackTrace()
                    Toast.makeText(this, "Error saving file: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            // This launcher handles the result of the permission request
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    // Permission was just granted.
                    // If there's a pending save operation, execute it now.
                    pendingSave.value?.let {
                        performSave(it)
                        pendingSave.value = null // Clear the pending save
                    }
                } else {
                    // User denied the permission.
                    Toast.makeText(this, "Storage permission denied.", Toast.LENGTH_SHORT).show()
                }
            }

            MaterialTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 10.dp),
                ) {
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        DemoRadioGroup(preserveAspectRatio)
                        DemoDropdownMenu(options = figures.unzip().first, selectedIndex = figureIndex)
                        Button(onClick = {
                            // The button's only job is to prepare and show the dialog
                            filenameState.value = "plot-${figures[figureIndex.value].first}.png"
                            scaleState.value = "1.0"
                            dpiState.value = ""
                            widthState.value = ""
                            heightState.value = ""
                            selectedUnit.value = SizeUnit.PX
                            showSaveDialog.value = true
                        }, modifier = Modifier.padding(8.dp)) {
                            Text("Save")
                        }
                    }

                    val fig = figures[figureIndex.value].second
                    if (fig is Map<*, *>) {
                        PlotPanelRaw(
                            rawSpec = fig as MutableMap<String, Any>,
                            preserveAspectRatio = preserveAspectRatio.value,
                            modifier = Modifier.fillMaxSize(),
                            errorModifier = Modifier.padding(16.dp),
                            errorTextStyle = TextStyle(color = Color(0xFF700000)),
                            computationMessagesHandler = { messages ->
                                messages.forEach { println("[DEMO APP MESSAGE] $it") }
                            }
                        )
                    } else if (fig is Figure) {
                        PlotPanel(
                            figure = fig,
                            preserveAspectRatio = preserveAspectRatio.value,
                            modifier = Modifier.fillMaxSize()
                        ) { computationMessages ->
                            computationMessages.forEach { println("[DEMO APP MESSAGE] $it") }
                        }
                    }
                }

                if (showSaveDialog.value) {
                    SaveSettingsDialog(
                        filenameState = filenameState,
                        scaleState = scaleState,
                        dpiState = dpiState,
                        widthState = widthState,
                        heightState = heightState,
                        unitState = selectedUnit,
                        onDismiss = { showSaveDialog.value = false },
                        onSave = {
                            showSaveDialog.value = false
                            val fig = figures[figureIndex.value].second as? Figure ?: return@SaveSettingsDialog

                            // Package all settings into a data class for easy passing
                            val currentSave = PendingSave(
                                figure = fig,
                                filename = filenameState.value,
                                scale = scaleState.value.toDoubleOrNull(),
                                dpi = dpiState.value.toIntOrNull(),
                                w = widthState.value.toDoubleOrNull(),
                                h = heightState.value.toDoubleOrNull(),
                                unit = selectedUnit.value
                            )

                            // Launch a coroutine for the file operation
                            coroutineScope.launch {
                                try {
                                    // Attempt to save the file
                                    performSave(currentSave)
                                } catch (e: MissingStoragePermissionException) {
                                    // If ggsave fails because of a missing permission...
                                    println("Permission required: ${e.message}")
                                    // ...store the save operation details...
                                    pendingSave.value = currentSave
                                    // ...and launch the system's permission request dialog.
                                    permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                }
                            }
                        }
                    )
                }

                savedImagePath.value?.let { path ->
                    ImagePreviewDialog(imagePath = path) {
                        savedImagePath.value = null
                    }
                }
            }
        }
    }
}

/**
 * Notifies the system's MediaStore that a new file has been created.
 */
private fun scanFileToShowInGallery(context: Context, filePath: String) {
    MediaScannerConnection.scanFile(context, arrayOf(filePath), null) { path, uri ->
        println("MediaScanner scanned path: $path, uri: $uri")
    }
}


/**
 * A dialog for setting image save options.
 */
@Composable
fun SaveSettingsDialog(
    filenameState: MutableState<String>,
    scaleState: MutableState<String>,
    dpiState: MutableState<String>,
    widthState: MutableState<String>,
    heightState: MutableState<String>,
    unitState: MutableState<SizeUnit>,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save Plot Options") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = filenameState.value,
                    onValueChange = { filenameState.value = it },
                    label = { Text("Filename") },
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = scaleState.value,
                    onValueChange = { scaleState.value = it },
                    label = { Text("Scale") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = dpiState.value,
                    onValueChange = { dpiState.value = it },
                    label = { Text("DPI (optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = widthState.value,
                        onValueChange = { widthState.value = it },
                        label = { Text("Width") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = heightState.value,
                        onValueChange = { heightState.value = it },
                        label = { Text("Height") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                UnitDropdown(selectedUnit = unitState)
            }
        },
        confirmButton = { Button(onClick = onSave) { Text("Save") } },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } }
    )
}

/**
 * A dropdown menu for selecting the size unit.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UnitDropdown(selectedUnit: MutableState<SizeUnit>) {
    var expanded by remember { mutableStateOf(false) }
    val units = SizeUnit.entries

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.padding(top = 8.dp)
    ) {
        OutlinedTextField(
            value = selectedUnit.value.value,
            onValueChange = {},
            readOnly = true,
            label = { Text("Unit") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            units.forEach { unit ->
                DropdownMenuItem(
                    onClick = {
                        selectedUnit.value = unit
                        expanded = false
                    }
                ) {
                    Text(unit.value)
                }
            }
        }
    }
}

/**
 * A full-screen dialog to display the saved image and its resolution.
 */
@Composable
fun ImagePreviewDialog(imagePath: String, onDismiss: () -> Unit) {
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var resolution by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(imagePath) {
        isLoading = true
        withContext(Dispatchers.IO) {
            try {
                val bitmap = BitmapFactory.decodeFile(imagePath)
                if (bitmap != null) {
                    resolution = bitmap.width to bitmap.height
                    imageBitmap = bitmap.asImageBitmap()
                } else {
                    resolution = null
                    imageBitmap = null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                resolution = null
                imageBitmap = null
            }
        }
        isLoading = false
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator()
                imageBitmap != null -> {
                    Image(
                        bitmap = imageBitmap!!,
                        contentDescription = "Saved plot preview",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentScale = ContentScale.Fit
                    )

                    resolution?.let { (width, height) ->
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(24.dp)
                                .background(
                                    color = Color.Black.copy(alpha = 0.6f),
                                    shape = MaterialTheme.shapes.medium
                                )
                        ) {
                            Text(
                                text = "$width x $height px",
                                color = Color.White,
                                style = MaterialTheme.typography.body1,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                else -> Text("Failed to load image", color = Color.White)
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Text("Close")
            }
        }
    }
}