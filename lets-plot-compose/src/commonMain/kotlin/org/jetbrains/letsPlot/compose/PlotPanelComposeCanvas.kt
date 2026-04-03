/*
 * Copyright (c) 2026. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import org.jetbrains.letsPlot.commons.geometry.DoubleVector
import org.jetbrains.letsPlot.commons.logging.PortableLogging
import org.jetbrains.letsPlot.commons.registration.Registration
import org.jetbrains.letsPlot.core.spec.config.PlotConfig
import org.jetbrains.letsPlot.core.spec.front.SpecOverrideUtil.applySpecOverride
import org.jetbrains.letsPlot.core.util.MonolithicCommon.processRawSpecs
import org.jetbrains.letsPlot.core.util.PlotThemeHelper
import org.jetbrains.letsPlot.core.util.sizing.SizingPolicy.Companion.fitContainerSize
import org.jetbrains.letsPlot.raster.view.PlotCanvasDrawable

private val LOG = PortableLogging.logger(name = "[PlotPanelRaw]")
private const val logRecompositions = false

@Suppress("FunctionName")
@Composable
internal fun PlotPanelComposeCanvas(
    rawSpec: MutableMap<String, Any>,
    figureModel: PlotFigureModel,
    preserveAspectRatio: Boolean,
    modifier: Modifier,
    errorTextStyle: TextStyle,
    errorModifier: Modifier,
    computationMessagesHandler: (List<String>) -> Unit
) {
    if (logRecompositions) {
        println("PlotPanelRaw: recomposition")
    }

    val density = LocalDensity.current.density
    val composeMouseEventMapper = remember { ComposeMouseEventMapper() }

    val processedPlotSpec = remember(rawSpec.hashCode()) {
        processRawSpecs(rawSpec, frontendOnly = false)
    }

    var panelSize by remember { mutableStateOf(DoubleVector.ZERO) }
    var plotPosition by remember { mutableStateOf(DoubleVector.ZERO) }
    var dispatchComputationMessages by remember { mutableStateOf(true) }
    val specOverrideState by figureModel.specOverrideState
    var errorMessage: String? by remember(processedPlotSpec, panelSize) { mutableStateOf(null) }
    var redrawTrigger by remember { mutableIntStateOf(0) }

    val plotDrawable = remember(errorMessage) {
        PlotCanvasDrawable().apply {
            mouseEventPeer.addEventSource(composeMouseEventMapper)
        }
    }

    val plotComponentRegistrations = remember(plotDrawable) {
        createPlatformPlotComponentRegistration(plotDrawable) { redrawTrigger++ }
    }

    val finalModifier = if (errorMessage != null) {
        modifier.background(Color.LightGray)
    } else if (containsBackground(modifier)) {
        modifier
    } else {
        val lpColor = PlotThemeHelper.plotBackground(processedPlotSpec)
        val lpBackground = Color(lpColor.red, lpColor.green, lpColor.blue, lpColor.alpha)
        modifier.background(lpBackground)
    }

    DisposableEffect(plotComponentRegistrations) {
        onDispose {
            try {
                plotComponentRegistrations.dispose()
            } catch (e: Exception) {
                LOG.error(e) { "plotComponentRegistrations.dispose() failed: ${e.message}" }
            }
        }
    }

    DisposableEffect(plotDrawable, figureModel) {
        onDispose {
            if (figureModel.toolEventDispatcher == plotDrawable.toolEventDispatcher) {
                figureModel.toolEventDispatcher = null
            }
        }
    }

    Column(modifier = finalModifier) {
        PlatformPlotToolbar(processedPlotSpec, figureModel)

        Box(
            modifier = finalModifier
                .weight(1f)
                .fillMaxWidth()
                .onSizeChanged { newSize ->
                    panelSize = DoubleVector(newSize.width / density, newSize.height / density)
                }
        ) {
            val errMsg = errorMessage
            if (errMsg != null) {
                BasicTextField(
                    value = errMsg,
                    onValueChange = { },
                    readOnly = true,
                    textStyle = errorTextStyle,
                    modifier = errorModifier
                )
            } else {
                LaunchedEffect(panelSize, processedPlotSpec, specOverrideState, preserveAspectRatio) {
                    if (PlotConfig.isFailure(processedPlotSpec)) {
                        errorMessage = PlotConfig.getErrorMessage(processedPlotSpec)
                        return@LaunchedEffect
                    }

                    runCatching {
                        if (panelSize != DoubleVector.ZERO) {
                            val plotSpec = applySpecOverride(processedPlotSpec, specOverrideState).toMutableMap()

                            plotDrawable.update(plotSpec, fitContainerSize(preserveAspectRatio)) { messages ->
                                if (dispatchComputationMessages) {
                                    dispatchComputationMessages = false
                                    computationMessagesHandler(messages)
                                }
                            }

                            figureModel.toolEventDispatcher = plotDrawable.toolEventDispatcher

                            plotPosition = DoubleVector(
                                maxOf(0.0, (panelSize.x - plotDrawable.size.x) / 2.0),
                                maxOf(0.0, (panelSize.y - plotDrawable.size.y) / 2.0)
                            )

                            composeMouseEventMapper.setOffset(plotPosition.x.toFloat(), plotPosition.y.toFloat())
                            redrawTrigger++
                        }
                    }.getOrElse { e ->
                        errorMessage = "${e.message}"
                        return@LaunchedEffect
                    }
                }

                Canvas(
                    modifier = modifier
                        .fillMaxSize()
                        .plotPointerHoverIcon()
                        .onSizeChanged { size ->
                            plotDrawable.resize(size.width / density, size.height / density)
                        }
                        .pointerInput(composeMouseEventMapper, composeMouseEventMapper)
                ) {
                    redrawTrigger
                    paintPlatformPlot(plotDrawable, density, plotPosition)
                }
            }
        }
    }
}

@Composable
internal expect fun PlatformPlotToolbar(
    processedPlotSpec: MutableMap<String, Any>,
    figureModel: PlotFigureModel
)

internal expect fun Modifier.plotPointerHoverIcon(): Modifier

internal expect fun createPlatformPlotComponentRegistration(
    plotDrawable: PlotCanvasDrawable,
    onRepaintRequested: () -> Unit
): Registration

internal expect fun DrawScope.paintPlatformPlot(
    plotDrawable: PlotCanvasDrawable,
    density: Float,
    plotPosition: DoubleVector
)
