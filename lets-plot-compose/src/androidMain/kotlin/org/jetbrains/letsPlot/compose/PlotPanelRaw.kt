/*
 * Copyright (c) 2025 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.viewinterop.AndroidView
import org.jetbrains.letsPlot.android.canvas.CanvasView2
import org.jetbrains.letsPlot.commons.logging.PortableLogging
import org.jetbrains.letsPlot.core.spec.config.PlotConfig
import org.jetbrains.letsPlot.core.util.MonolithicCommon.processRawSpecs
import org.jetbrains.letsPlot.core.util.PlotThemeHelper
import org.jetbrains.letsPlot.core.util.sizing.SizingPolicy
import org.jetbrains.letsPlot.raster.view.PlotCanvasFigure2

//import org.jetbrains.letsPlot.compose.util.NaiveLogger

//private val LOG = NaiveLogger("PlotPanel")
private val LOG = PortableLogging.logger(name = "[PlotPanelRaw]")

// This flag is mentioned in the ComposeMinDemoActivity.kt
// In a case of changes update the comment there too.
private const val logRecompositions = false

@Suppress("FunctionName")
@Composable
actual fun PlotPanelRaw(
    rawSpec: MutableMap<String, Any>,
    preserveAspectRatio: Boolean,
    modifier: Modifier,
    errorTextStyle: TextStyle,
    errorModifier: Modifier,
    computationMessagesHandler: (List<String>) -> Unit
) {
    if (logRecompositions) {
        println("PlotPanel: recomposition")
    }

    var plotCanvasFigure: PlotCanvasFigure2? by remember { mutableStateOf(null) }
    val sizingPolicy = SizingPolicy.fitContainerSize(preserveAspectRatio)

    // Cache processed plot spec to avoid reprocessing the same raw spec on every recomposition.

    // Note: Use remember(rawSpec.hashCode()), to bypass the equality check and use the content hash directly.
    // The issue was that remember(rawSpec) uses some kind of comparison (equals()?) which somehow not working for `MutableMap`.
    val processedPlotSpec = remember(rawSpec.hashCode()) {
        processRawSpecs(rawSpec, frontendOnly = false)
    }
    var errorMessage: String? by remember { mutableStateOf(null) }

    if (PlotConfig.isFailure(processedPlotSpec)) {
        errorMessage = PlotConfig.getErrorMessage(processedPlotSpec)
    }

    // Background
    val finalModifier = if (errorMessage != null) {
        modifier.background(Color.LightGray)
    } else {
        if (containsBackground(modifier)) {
            // Do not change the user-defined background
            modifier
        } else {
            // Use background color from the plot theme
            val lpColor = PlotThemeHelper.plotBackground(processedPlotSpec)
            val lpBackground = Color(lpColor.red, lpColor.green, lpColor.blue, lpColor.alpha)
            modifier.background(lpBackground)
        }
    }

    LaunchedEffect(processedPlotSpec, sizingPolicy, computationMessagesHandler) {
        runCatching {
            plotCanvasFigure?.update(processedPlotSpec, sizingPolicy, computationMessagesHandler)
                ?: LOG.info { "Error updating plot figure - plotCanvasFigure is null" }
        }.onFailure { e ->
            errorMessage = e.message ?: "Unknown error: ${e::class.simpleName}"
            LOG.error(e) { "Error updating plot figure" }
        }

    }

    errorMessage?.let { errMsg ->
        // Reset the figure to resolve the 'Registration already removed' error.
        // On error, the CanvasView is removed and the plotCanvasFigure changes state to 'detached',
        // meaning it cannot be reused.
        plotCanvasFigure = null

        // Show error message
        BasicTextField(
            value = errMsg,
            onValueChange = { },
            readOnly = true,
            textStyle = errorTextStyle,
            modifier = errorModifier
        )
    } ?: run {
        @Suppress("COMPOSE_APPLIER_CALL_MISMATCH") // Gemini says that this is a false positive
        AndroidView(
            modifier = finalModifier,
            factory = { ctx ->
                plotCanvasFigure = plotCanvasFigure ?: PlotCanvasFigure2()

                CanvasView2(ctx).apply {
                    figure = plotCanvasFigure
                    onError = { e ->
                        @Suppress("AssignedValueIsNeverRead")
                        errorMessage = e.message ?: "Unknown error: ${e::class.simpleName}"
                        LOG.error(e) { "Error in CanvasView" }
                    }
                }
            }
        )
    }
}

private fun containsBackground(modifier: Modifier): Boolean {
    return modifier.foldIn(false) { hasBg, element ->
        hasBg || element.toString().contains("BackgroundElement")
    }
}
