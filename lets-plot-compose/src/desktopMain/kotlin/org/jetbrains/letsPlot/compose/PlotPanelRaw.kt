/*
 * Copyright (c) 2025 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import org.jetbrains.letsPlot.commons.geometry.DoubleVector
import org.jetbrains.letsPlot.commons.logging.PortableLogging
import org.jetbrains.letsPlot.compose.desktop.PlotContainer
import org.jetbrains.letsPlot.compose.desktop.SvgViewPanel
import org.jetbrains.letsPlot.core.plot.builder.interact.tools.FigureModelHelper
import org.jetbrains.letsPlot.core.spec.Option.Meta.Kind.GG_TOOLBAR
import org.jetbrains.letsPlot.core.spec.config.PlotConfig
import org.jetbrains.letsPlot.core.spec.front.SpecOverrideUtil
import org.jetbrains.letsPlot.core.util.MonolithicCommon.processRawSpecs
import org.jetbrains.letsPlot.core.util.PlotThemeHelper
import org.jetbrains.letsPlot.core.util.sizing.SizingPolicy
import org.jetbrains.letsPlot.skia.builder.MonolithicSkia

//import org.jetbrains.letsPlot.compose.util.NaiveLogger

//private val LOG = NaiveLogger("PlotPanel")
private val LOG = PortableLogging.logger(name = "[PlotPanelRaw]")

private const val logRecompositions = false

private enum class RenderMode {
    SKIA_MAPPER,
    SKIA_CANVAS_PLOT_RASTER,
    BOTH
}

private val RENDER_MODE = RenderMode.BOTH

@Composable
actual fun PlotPanelRaw(
    rawSpec: MutableMap<String, Any>,
    preserveAspectRatio: Boolean,
    modifier: Modifier,
    errorTextStyle: TextStyle,
    errorModifier: Modifier,
    computationMessagesHandler: (List<String>) -> Unit
) {
    Row(modifier = modifier) {
        when (RENDER_MODE) {
            RenderMode.BOTH -> {
                Column(modifier = Modifier.weight(1f)) {
                    androidx.compose.material.Text(text = "Skia mapper")
                    PlotPanelRawOld(
                        rawSpec = rawSpec,
                        preserveAspectRatio = preserveAspectRatio,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        errorTextStyle = errorTextStyle,
                        errorModifier = errorModifier,
                        computationMessagesHandler = computationMessagesHandler
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    androidx.compose.material.Text(text = "Skia Canvas + plot_raster")
                    PlotPanelRawNew(
                        rawSpec = rawSpec,
                        preserveAspectRatio = preserveAspectRatio,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        errorTextStyle = errorTextStyle,
                        errorModifier = errorModifier,
                        computationMessagesHandler = computationMessagesHandler
                    )
                }
            }

            RenderMode.SKIA_CANVAS_PLOT_RASTER -> {
                PlotPanelRawNew(
                    rawSpec = rawSpec,
                    preserveAspectRatio = preserveAspectRatio,
                    modifier = modifier,
                    errorTextStyle = errorTextStyle,
                    errorModifier = errorModifier,
                    computationMessagesHandler = computationMessagesHandler
                )
            }

            RenderMode.SKIA_MAPPER -> {
                PlotPanelRawOld(
                    rawSpec = rawSpec,
                    preserveAspectRatio = preserveAspectRatio,
                    modifier = modifier,
                    errorTextStyle = errorTextStyle,
                    errorModifier = errorModifier,
                    computationMessagesHandler = computationMessagesHandler
                )
            }
        }
    }
}

@Suppress("FunctionName")
@Composable
fun PlotPanelRawOld(
    rawSpec: MutableMap<String, Any>,
    preserveAspectRatio: Boolean,
    modifier: Modifier,
    errorTextStyle: TextStyle,
    errorModifier: Modifier,
    computationMessagesHandler: (List<String>) -> Unit
) {
    if (logRecompositions) {
        println("PlotPanelRaw: recomposition")
    }

    // Update density on each recomposition to handle monitor DPI changes (e.g., drag between HIDPI/regular monitor)
    val density = LocalDensity.current.density.toDouble()

    // Cache processed plot spec to avoid reprocessing the same raw spec on every recomposition.

    // Note: Use remember(rawSpec.hashCode()), to bypass the equality check and use the content hash directly.
    // The issue was that remember(rawSpec) uses some kind of comparison (equals()?) which somehow not working for `MutableMap`.
    val processedPlotSpec = remember(rawSpec.hashCode()) {
        processRawSpecs(rawSpec, frontendOnly = false)
    }

    var panelSize by remember { mutableStateOf(DoubleVector.ZERO) }
    var dispatchComputationMessages by remember { mutableStateOf(true) }
    var specOverrideList by remember { mutableStateOf(emptyList<Map<String, Any>>()) }
    var plotFigureModel by remember { mutableStateOf<PlotFigureModel?>(null) }


    var errorMessage: String? by remember(processedPlotSpec) { mutableStateOf(null) }

    // Reset the old plot on error to prevent blinking
    // We can't reset PlotContainer using updateViewmodel(), so we create a new one.
    val plotContainer = remember(errorMessage) { PlotContainer() }

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


    DisposableEffect(plotContainer) {
        onDispose {
            // Try/catch to ensure that any exception in dispose() does not break the Composable lifecycle
            // Otherwise, the app window gets unclosable.
            try {
                plotContainer.dispose()
            } catch (e: Exception) {
                LOG.error(e) { "PlotContainer.dispose() failed" }
            }
        }
    }

    Column(modifier = finalModifier) {
        if (plotFigureModel != null && GG_TOOLBAR in processedPlotSpec) {
            PlotToolbar(plotFigureModel!!)
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
                            val plotSpec =
                                SpecOverrideUtil.applySpecOverride(processedPlotSpec, specOverrideList).toMutableMap()

                            val viewModel = MonolithicSkia.buildPlotFromProcessedSpecs(
                                plotSpec = plotSpec,
                                containerSize = panelSize,
                                sizingPolicy = SizingPolicy.fitContainerSize(preserveAspectRatio)
                            ) { messages ->
                                if (dispatchComputationMessages) {
                                    // do once
                                    dispatchComputationMessages = false
                                    computationMessagesHandler(messages)
                                }
                            }


                            if (plotFigureModel == null) {
                                plotFigureModel = PlotFigureModel(
                                    onUpdateView = { specOverride ->
                                        specOverrideList = FigureModelHelper.updateSpecOverrideList(
                                            specOverrideList = specOverrideList,
                                            newSpecOverride = specOverride
                                        )
                                    }
                                )
                            }

                            plotFigureModel!!.toolEventDispatcher = viewModel.toolEventDispatcher

                            val plotWidth = viewModel.svg.width().get() ?: panelSize.x
                            val plotHeight = viewModel.svg.height().get() ?: panelSize.y

                            // Calculate centering position in physical pixels
                            // Both panelSize and plot dimensions are in physical pixels
                            val position = DoubleVector(
                                maxOf(0.0, (panelSize.x - plotWidth) / 2.0),
                                maxOf(0.0, (panelSize.y - plotHeight) / 2.0)
                            )

                            plotContainer.updateViewModel(viewModel, position, density.toFloat())
                        }
                    }.getOrElse { e ->
                        errorMessage = "${e.message}"
                        return@LaunchedEffect
                    }
                }

                SvgViewPanel(
                    svgView = plotContainer.svgView,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

private fun containsBackground(modifier: Modifier): Boolean {
    return modifier.foldIn(false) { hasBg, element ->
        hasBg || element.toString().contains("BackgroundElement")
    }
}
