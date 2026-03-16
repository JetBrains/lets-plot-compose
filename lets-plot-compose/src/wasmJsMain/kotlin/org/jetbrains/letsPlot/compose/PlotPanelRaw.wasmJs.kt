package org.jetbrains.letsPlot.compose

@Suppress(names = ["FunctionName"])
@androidx.compose.runtime.Composable
actual fun PlotPanelRaw(
    rawSpec: MutableMap<String, Any>,
    figureModel: PlotFigureModel?,
    preserveAspectRatio: Boolean,
    modifier: androidx.compose.ui.Modifier,
    errorTextStyle: androidx.compose.ui.text.TextStyle,
    errorModifier: androidx.compose.ui.Modifier,
    computationMessagesHandler: (List<String>) -> Unit
) {
}