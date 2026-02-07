package org.jetbrains.letsPlot.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import org.jetbrains.letsPlot.core.plot.builder.interact.tools.FigureModel
import org.jetbrains.letsPlot.core.spec.Option
import org.jetbrains.letsPlot.core.spec.config.PlotConfig
import org.jetbrains.letsPlot.core.spec.front.SpecOverrideUtil
import org.jetbrains.letsPlot.core.util.MonolithicCommon
import org.jetbrains.letsPlot.core.util.PlotThemeHelper
import org.jetbrains.letsPlot.core.util.sizing.SizingPolicy
import org.jetbrains.letsPlot.skia.builder.MonolithicSkia

private val LOG = PortableLogging.logger(name = "[PlotPanelRaw]")

private const val logRecompositions = false

@Suppress("FunctionName")
@Composable
fun PlotPanelSwingComponent(
    rawSpec: MutableMap<String, Any>,
    figureModel: FigureModel,
    preserveAspectRatio: Boolean,
    modifier: Modifier,
    errorTextStyle: TextStyle,
    errorModifier: Modifier,
    computationMessagesHandler: (List<String>) -> Unit
) {
    if (logRecompositions) {
        println("PlotPanelSwingComponent: recomposition")
    }

    // Update density on each recomposition to handle monitor DPI changes (e.g., drag between HIDPI/regular monitor)
    val density = LocalDensity.current.density.toDouble()

    // Cache processed plot spec to avoid reprocessing the same raw spec on every recomposition.

    // Note: Use remember(rawSpec.hashCode()), to bypass the equality check and use the content hash directly.
    // The issue was that remember(rawSpec) uses some kind of comparison (equals()?) which somehow not working for `MutableMap`.
    val processedPlotSpec = remember(rawSpec.hashCode()) {
        MonolithicCommon.processRawSpecs(rawSpec, frontendOnly = false)
    }

    var panelSize by remember { mutableStateOf(DoubleVector.ZERO) }
    var dispatchComputationMessages by remember { mutableStateOf(true) }

    // Observe spec override list from figureModel
    val specOverrideList by (figureModel as PlotFigureModel).specOverrideListState

    var errorMessage: String? by remember(processedPlotSpec, panelSize) { mutableStateOf(null) }

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
                LOG.error(e) { "plotContainer.dispose() failed: ${e.message}" }
            }
        }
    }

    Column(modifier = finalModifier) {
        if (Option.Meta.Kind.GG_TOOLBAR in processedPlotSpec) {
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

                            // Connect the figure model to the plot component
                            figureModel.toolEventDispatcher = viewModel.toolEventDispatcher

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
