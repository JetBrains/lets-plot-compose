package demo.plot

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import demo.plot.ui.DemoDropdownMenu
import demo.plot.ui.DemoRadioGroup
import demo.plot.ui.ImagePreviewDialog
import demo.plot.ui.SavePlotDialog
import kotlinx.coroutines.launch
import org.jetbrains.letsPlot.Figure
import org.jetbrains.letsPlot.compose.PlotFormat
import org.jetbrains.letsPlot.compose.PlotPanel
import org.jetbrains.letsPlot.compose.PlotPanelRaw
import org.jetbrains.letsPlot.compose.ggsave
import org.jetbrains.letsPlot.core.util.PlotExportCommon.SizeUnit
import plotSpec.*

private data class SaveParameters(
    val figure: Figure,
    val format: PlotFormat,
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

            val context = LocalContext.current
            val coroutineScope = rememberCoroutineScope()

            val preserveAspectRatio = rememberSaveable { mutableStateOf(true) }
            val figureIndex = rememberSaveable { mutableStateOf(3) }
            val showSaveDialog = remember { mutableStateOf(false) }
            val savedPlotInfo = remember { mutableStateOf<Pair<Uri, PlotFormat>?>(null) }
            val pendingSave = remember { mutableStateOf<SaveParameters?>(null) }

            val filePickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.CreateDocument("*/*")
            ) { uri: Uri? ->
                if (uri == null) {
                    pendingSave.value = null
                    return@rememberLauncherForActivityResult
                }

                pendingSave.value?.apply {
                    coroutineScope.launch {
                        try {
                            ggsave(uri, figure, format, scale, dpi, w, h, unit, context)
                            Toast.makeText(context, "Saved successfully", Toast.LENGTH_LONG).show()
                            // Store the Uri and the format together
                            savedPlotInfo.value = Pair(uri, format)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(context, "Error saving file: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            pendingSave.value = null
                        }
                    }
                }
            }

            // Dialog input fields state
            val scaleState = remember { mutableStateOf("1.0") }
            val dpiState = remember { mutableStateOf("") }
            val widthState = remember { mutableStateOf("") }
            val heightState = remember { mutableStateOf("") }
            val selectedUnit = remember { mutableStateOf(SizeUnit.PX) }
            val selectedFormat = remember { mutableStateOf(PlotFormat.PNG) }

            MaterialTheme {
                Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        DemoRadioGroup(preserveAspectRatio)
                        DemoDropdownMenu(options = figures.unzip().first, selectedIndex = figureIndex)
                        Button(onClick = {
                            selectedFormat.value = PlotFormat.PNG
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
                    SavePlotDialog(
                        formatState = selectedFormat,
                        scaleState = scaleState,
                        dpiState = dpiState,
                        widthState = widthState,
                        heightState = heightState,
                        unitState = selectedUnit,
                        onDismiss = { showSaveDialog.value = false },
                        onSave = {
                            showSaveDialog.value = false
                            val fig = figures[figureIndex.value].second as? Figure ?: return@SavePlotDialog

                            val rawName = figures[figureIndex.value].first
                            val sanitizedName = sanitizeForFilename(rawName)
                            val suggestedFilename = "plot-${sanitizedName}.${selectedFormat.value.ext}"

                            pendingSave.value = SaveParameters(
                                figure = fig,
                                format = selectedFormat.value,
                                scale = scaleState.value.toDoubleOrNull(),
                                dpi = dpiState.value.toIntOrNull(),
                                w = widthState.value.toDoubleOrNull(),
                                h = heightState.value.toDoubleOrNull(),
                                unit = selectedUnit.value
                            )
                            filePickerLauncher.launch(suggestedFilename)
                        }
                    )
                }

                savedPlotInfo.value?.let { (uri, format) ->
                    ImagePreviewDialog(uri = uri, format = format) {
                        savedPlotInfo.value = null
                    }
                }
            }
        }
    }
}

private fun sanitizeForFilename(name: String): String {
    val nonAlphaNumeric = Regex("[^a-z0-9-] A")
    return name.lowercase()
        .replace(" ", "-")
        .replace(nonAlphaNumeric, "")
}
