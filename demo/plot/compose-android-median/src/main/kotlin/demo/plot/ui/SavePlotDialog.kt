package demo.plot.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.jetbrains.letsPlot.compose.PlotFormat
import org.jetbrains.letsPlot.core.util.PlotExportCommon.SizeUnit

@Composable
fun SavePlotDialog(
    formatState: MutableState<PlotFormat>,
    scaleState: MutableState<String>,
    dpiState: MutableState<String>,
    widthState: MutableState<String>,
    heightState: MutableState<String>,
    unitState: MutableState<SizeUnit>,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val isDpiEnabled = unitState.value != SizeUnit.PX
    LaunchedEffect(isDpiEnabled) {
        if (!isDpiEnabled) {
            dpiState.value = ""
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save Plot Options") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FormatDropdown(
                        selectedFormat = formatState,
                        onFormatChange = { newFormat -> formatState.value = newFormat },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = scaleState.value,
                        onValueChange = { scaleState.value = it },
                        label = { Text("Scale") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
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
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UnitDropdown(selectedUnit = unitState, modifier = Modifier.weight(1f))
                    OutlinedTextField(
                        value = dpiState.value,
                        onValueChange = { dpiState.value = it },
                        label = { Text("DPI") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        enabled = isDpiEnabled,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = { Button(onClick = onSave) { Text("Save") } },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FormatDropdown(
    selectedFormat: MutableState<PlotFormat>,
    onFormatChange: (PlotFormat) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val formats = PlotFormat.entries.toTypedArray()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedFormat.value.ext,
            onValueChange = {},
            readOnly = true,
            label = { Text("Format") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            formats.forEach { format ->
                DropdownMenuItem(onClick = {
                    onFormatChange(format)
                    expanded = false
                }) {
                    Text(format.ext)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UnitDropdown(selectedUnit: MutableState<SizeUnit>, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val units = SizeUnit.entries.toTypedArray()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedUnit.value.name.lowercase(),
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
                    Text(unit.name.lowercase())
                }
            }
        }
    }
}
