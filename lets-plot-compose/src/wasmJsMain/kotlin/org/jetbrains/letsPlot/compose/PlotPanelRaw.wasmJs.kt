package org.jetbrains.letsPlot.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember

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
    val actualFigureModel = figureModel ?: remember { PlotFigureModel() }

    // Dispose internally created figureModel when this component is removed
    if (figureModel == null) {
        DisposableEffect(actualFigureModel) {
            onDispose {
                actualFigureModel.dispose()
            }
        }
    }

    Row(modifier = modifier) {
        PlotPanelComposeCanvas(
            rawSpec = rawSpec,
            figureModel = actualFigureModel,
            preserveAspectRatio = preserveAspectRatio,
            modifier = modifier,
            errorTextStyle = errorTextStyle,
            errorModifier = errorModifier,
            computationMessagesHandler = computationMessagesHandler
        )
    }
}