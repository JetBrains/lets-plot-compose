/*
 * Copyright (c) 2025 JetBrains s.r.o.
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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import org.jetbrains.letsPlot.commons.geometry.DoubleVector
import org.jetbrains.letsPlot.commons.logging.PortableLogging
import org.jetbrains.letsPlot.commons.registration.CompositeRegistration
import org.jetbrains.letsPlot.commons.registration.Registration
import org.jetbrains.letsPlot.compose.canvas.SkiaCanvasPeer
import org.jetbrains.letsPlot.compose.canvas.SkiaContext2d
import org.jetbrains.letsPlot.compose.canvas.SkiaFontManager
import org.jetbrains.letsPlot.core.plot.builder.interact.tools.FigureModel
import org.jetbrains.letsPlot.core.spec.Option.Meta.Kind.GG_TOOLBAR
import org.jetbrains.letsPlot.core.spec.config.PlotConfig
import org.jetbrains.letsPlot.core.spec.front.SpecOverrideUtil.applySpecOverride
import org.jetbrains.letsPlot.core.util.MonolithicCommon.processRawSpecs
import org.jetbrains.letsPlot.core.util.PlotThemeHelper
import org.jetbrains.letsPlot.core.util.sizing.SizingPolicy.Companion.fitContainerSize
import org.jetbrains.letsPlot.raster.view.PlotCanvasFigure2
import java.awt.Cursor
import java.awt.Desktop
import java.net.URI

//import org.jetbrains.letsPlot.compose.util.NaiveLogger

//private val LOG = NaiveLogger("PlotPanel")
private val LOG = PortableLogging.logger(name = "[PlotPanelRaw2]")

private const val logRecompositions = false

@Suppress("FunctionName")
@Composable
fun PlotPanelComposeCanvas(
    rawSpec: MutableMap<String, Any>,
    figureModel: FigureModel,
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
    val skiaFontManager = remember { SkiaFontManager() }
    val composeMouseEventMapper = remember { ComposeMouseEventMapper() }
    // Update density on each recomposition to handle monitor DPI changes (e.g., drag between HIDPI/regular monitor)

    // Cache processed plot spec to avoid reprocessing the same raw spec on every recomposition.
    // Note: Use remember(rawSpec.hashCode()), to bypass the equality check and use the content hash directly.
    // The issue was that remember(rawSpec) uses some kind of comparison (equals()?) which somehow not working for `MutableMap`.
    val processedPlotSpec = remember(rawSpec.hashCode()) {
        processRawSpecs(rawSpec, frontendOnly = false)
    }

    var panelSize by remember { mutableStateOf(DoubleVector.ZERO) }
    var plotPosition by remember { mutableStateOf(DoubleVector.ZERO) }
    var dispatchComputationMessages by remember { mutableStateOf(true) }

    // Observe spec override list from figureModel
    val specOverrideList by (figureModel as PlotFigureModel).specOverrideListState

    var errorMessage: String? by remember(processedPlotSpec, panelSize) { mutableStateOf(null) }

    var redrawTrigger by remember { mutableStateOf(0) }

    val skiaCanvasPeer = remember { SkiaCanvasPeer() }

    // Reset the old plot on error to prevent blinking
    // We can't reset PlotContainer using updateViewmodel(), so we create a new one.
    val plotCanvasFigure2 = remember(errorMessage) {
        PlotCanvasFigure2().apply {
            eventPeer.addEventSource(composeMouseEventMapper)
        }
    }

    val reg = remember(plotCanvasFigure2) {
        plotCanvasFigure2.onHrefClick(::browseLink)
        CompositeRegistration(
            // trigger recomposition on repaint request
            plotCanvasFigure2.onRepaintRequested { redrawTrigger++ },
            plotCanvasFigure2.mapToCanvas(skiaCanvasPeer),
            Registration.onRemove {
                plotCanvasFigure2.onHrefClick(handler = {})
            }
        )
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


    DisposableEffect(reg) {
        onDispose {
            // Try/catch to ensure that any exception in dispose() does not break the Composable lifecycle
            // Otherwise, the app window gets unclosable.
            try {
                reg.dispose()
                //plotCanvasFigure2.dispose()
            } catch (e: Exception) {
                LOG.error(e) { "reg.dispose() failed" }
            }
        }
    }

    Column(modifier = finalModifier) {
        if (GG_TOOLBAR in processedPlotSpec) {
            PlotToolbar(figureModel)
        }

        Box(
            modifier = finalModifier
                .weight(1f) // Take the remaining vertical space
                .fillMaxWidth() // Fill available width
                .onSizeChanged { newSize ->
                    // Convert logical pixels (from Compose layout) to physical pixels (plot SVG pixels)
                    panelSize = DoubleVector(newSize.width / density, newSize.height / density)
                }
        ) {
            val errMsg = errorMessage
            if (errMsg != null) {
                // Show error message
                BasicTextField(
                    value = errMsg,
                    onValueChange = { },
                    readOnly = true,
                    textStyle = errorTextStyle,
                    modifier = errorModifier
                )
            } else {
                // Render the plot
                LaunchedEffect(panelSize, processedPlotSpec, specOverrideList, preserveAspectRatio) {

                    if (PlotConfig.isFailure(processedPlotSpec)) {
                        errorMessage = PlotConfig.getErrorMessage(processedPlotSpec)
                        return@LaunchedEffect
                    }

                    runCatching {
                        if (panelSize != DoubleVector.ZERO) {
                            val plotSpec = applySpecOverride(processedPlotSpec, specOverrideList).toMutableMap()

                            plotCanvasFigure2.update(plotSpec, fitContainerSize(preserveAspectRatio)) { messages ->
                                if (dispatchComputationMessages) {
                                    // do once
                                    dispatchComputationMessages = false
                                    computationMessagesHandler(messages)
                                }
                            }

                            // Connect the figure model to the plot component
                            figureModel.toolEventDispatcher = plotCanvasFigure2.toolEventDispatcher


                            val plotWidth = plotCanvasFigure2.size.x
                            val plotHeight = plotCanvasFigure2.size.y

                            // Calculate centering position in physical pixels
                            // Both panelSize and plot dimensions are in physical pixels
                            plotPosition = DoubleVector(
                                maxOf(0.0, (panelSize.x - plotWidth) / 2.0),
                                maxOf(0.0, (panelSize.y - plotHeight) / 2.0)
                            )

                            composeMouseEventMapper.setOffset(plotPosition.x.toFloat(), plotPosition.y.toFloat())

                            redrawTrigger++ // trigger repaint
                        }
                    }.getOrElse { e ->
                        errorMessage = "${e.message}"
                        return@LaunchedEffect
                    }
                }

                Canvas(
                    modifier = modifier
                        .fillMaxSize()
                        .pointerHoverIcon(PointerIcon(Cursor(Cursor.CROSSHAIR_CURSOR)))
                        .onSizeChanged { size ->
                            // Convert canvas logical pixels (from Compose layout) to physical pixels (plot SVG pixels)
                            plotCanvasFigure2.resize(size.width / density, size.height / density)
                        }
                        .pointerInput(composeMouseEventMapper, composeMouseEventMapper)
                ) {
                    // By reading redrawTrigger here, Compose knows to recompose
                    // this Canvas block whenever it changes.
                    redrawTrigger

                    val ctx = SkiaContext2d(drawContext.canvas.nativeCanvas, skiaFontManager)
                    ctx.scale(density.toDouble(), density.toDouble()) // logical → physical pixels

                    ctx.translate(plotPosition.x, plotPosition.y)
                    plotCanvasFigure2.paint(ctx)

                    ctx.dispose()
                }
            }
        }
    }
}

private fun browseLink(string: String) {
    try {
        val uri = URI(string)
        Desktop.getDesktop().browse(uri)
    } catch (e: Exception) {
        LOG.error(e) { "Failed to open link: $string (${e.message})" }
    }
}
