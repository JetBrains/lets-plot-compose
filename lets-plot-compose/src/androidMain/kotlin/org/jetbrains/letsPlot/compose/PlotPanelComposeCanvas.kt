/*
 * Copyright (c) 2025 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.compose

import android.view.PointerIcon.TYPE_CROSSHAIR
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import org.jetbrains.letsPlot.android.canvas.AndroidCanvasPeer
import org.jetbrains.letsPlot.android.canvas.AndroidContext2d
import org.jetbrains.letsPlot.commons.event.*
import org.jetbrains.letsPlot.commons.event.MouseEventSpec.*
import org.jetbrains.letsPlot.commons.geometry.DoubleVector
import org.jetbrains.letsPlot.commons.geometry.Vector
import org.jetbrains.letsPlot.commons.intern.observable.event.EventHandler
import org.jetbrains.letsPlot.commons.logging.PortableLogging
import org.jetbrains.letsPlot.commons.registration.CompositeRegistration
import org.jetbrains.letsPlot.commons.registration.Registration
import org.jetbrains.letsPlot.core.plot.builder.interact.tools.FigureModel
import org.jetbrains.letsPlot.core.spec.config.PlotConfig
import org.jetbrains.letsPlot.core.spec.front.SpecOverrideUtil.applySpecOverride
import org.jetbrains.letsPlot.core.util.MonolithicCommon.processRawSpecs
import org.jetbrains.letsPlot.core.util.PlotThemeHelper
import org.jetbrains.letsPlot.core.util.sizing.SizingPolicy.Companion.fitContainerSize
import org.jetbrains.letsPlot.raster.view.PlotCanvasFigure2
import kotlin.math.roundToInt

//import org.jetbrains.letsPlot.compose.util.NaiveLogger

//private val LOG = NaiveLogger("PlotPanel")
private val LOG = PortableLogging.logger(name = "[PlotPanelRaw]")

// This flag is mentioned in the ComposeMinDemoActivity.kt
// In a case of changes update the comment there too.
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

    val androidCanvasPeer = remember { AndroidCanvasPeer() }

    // Reset the old plot on error to prevent blinking
    // We can't reset PlotContainer using updateViewmodel(), so we create a new one.
    val plotCanvasFigure2 = remember(errorMessage) {
        PlotCanvasFigure2().apply {
            eventPeer.addEventSource(composeMouseEventMapper)
        }
    }

    val reg = remember(plotCanvasFigure2) {
        CompositeRegistration(
            // trigger recomposition on repaint request
            plotCanvasFigure2.onRepaintRequested { redrawTrigger++ },
            plotCanvasFigure2.mapToCanvas(androidCanvasPeer)
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
        //if (plotFigureModel != null && GG_TOOLBAR in processedPlotSpec) {
        //    PlotToolbar(plotFigureModel!!)
        //}

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
                        .pointerHoverIcon(PointerIcon(TYPE_CROSSHAIR))
                        .onSizeChanged { size ->
                            // Convert canvas logical pixels (from Compose layout) to physical pixels (plot SVG pixels)
                            plotCanvasFigure2.resize(size.width / density, size.height / density)
                        }
                        .pointerInput(composeMouseEventMapper, composeMouseEventMapper)
                ) {
                    // By reading redrawTrigger here, Compose knows to recompose
                    // this Canvas block whenever it changes.
                    redrawTrigger

                    val ctx = AndroidContext2d(drawContext.canvas.nativeCanvas, pixelDensity = 1.0)
                    ctx.scale(density.toDouble(), density.toDouble()) // logical → physical pixels

                    ctx.translate(plotPosition.x, plotPosition.y)
                    plotCanvasFigure2.paint(ctx)
                }
            }
        }
    }
}

private fun containsBackground(modifier: Modifier): Boolean {
    return modifier.foldIn(false) { hasBg, element ->
        hasBg || element.toString().contains("BackgroundElement")
    }
}

class ComposeMouseEventMapper : MouseEventSource, PointerInputEventHandler {
    private val mouseEventPeer = MouseEventPeer()
    private var dragging: Boolean = false
    private var clickCount: Int = 0
    private var lastClickTime: Long = 0
    private var offsetX: Float = 0f
    private var offsetY: Float = 0f

    fun setOffset(offsetX: Float, offsetY: Float) {
        this.offsetX = offsetX
        this.offsetY = offsetY
    }

    override fun addEventHandler(eventSpec: MouseEventSpec, eventHandler: EventHandler<MouseEvent>): Registration {
        return mouseEventPeer.addEventHandler(eventSpec, eventHandler)
    }

    override suspend fun PointerInputScope.invoke() {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()
                val change = event.changes.first()
                val position = change.position

                // Convert logical pixel coordinates to physical pixel coordinates for SVG interaction
                // 1. Scale down by density (logical → physical pixels)
                // 2. Subtract position offset (which is also in physical pixels)
                val adjustedX = ((position.x / density) - offsetX).roundToInt()
                val adjustedY = ((position.y / density) - offsetY).roundToInt()
                val vector = Vector(adjustedX, adjustedY)

                val mouseEvent = when {
                    change.pressed -> MouseEvent.leftButton(vector)
                    else -> MouseEvent.noButton(vector)
                }

                when (event.type) {
                    PointerEventType.Press -> {
                        val currentTime = System.currentTimeMillis()
                        clickCount = if (currentTime - lastClickTime < 300) {
                            clickCount + 1
                        } else {
                            1
                        }
                        lastClickTime = currentTime

                        mouseEventPeer.dispatch(MOUSE_PRESSED, mouseEvent)
                    }

                    PointerEventType.Release -> {
                        if (clickCount > 0 && !dragging) {
                            val pos = event.changes.first().position
                            dispatchClick(pos, clickCount, density.toDouble())
                            if (clickCount > 1) {
                                clickCount = 0 // Reset after a double click
                            }
                        }

                        dragging = false
                        mouseEventPeer.dispatch(MOUSE_RELEASED, mouseEvent)
                    }

                    PointerEventType.Move -> {
                        if (change.pressed) {
                            dragging = true
                            mouseEventPeer.dispatch(MOUSE_MOVED, mouseEvent)
                            //mouseEventPeer.dispatch(MOUSE_DRAGGED, mouseEvent)
                        } else {
                            mouseEventPeer.dispatch(MOUSE_MOVED, mouseEvent)
                        }
                    }

                    PointerEventType.Enter -> {
                        mouseEventPeer.dispatch(MOUSE_ENTERED, mouseEvent)
                    }

                    PointerEventType.Exit -> {
                        mouseEventPeer.dispatch(MOUSE_LEFT, mouseEvent)
                    }

                    PointerEventType.Scroll -> {
                        val scrollDelta = change.scrollDelta
                        val wheelMouseEvent = MouseWheelEvent(
                            x = vector.x,
                            y = vector.y,
                            button = Button.NONE,
                            modifiers = KeyModifiers.emptyModifiers(),
                            scrollAmount = scrollDelta.y.toDouble()
                        )
                        mouseEventPeer.dispatch(MOUSE_WHEEL_ROTATED, wheelMouseEvent)
                    }
                }
            }
        }
    }

    private fun dispatchClick(position: Offset, clickCount: Int, density: Double) {
        // Convert logical pixel coordinates to physical pixel coordinates for SVG interaction
        val adjustedX = ((position.x / density) - offsetX).roundToInt()
        val adjustedY = ((position.y / density) - offsetY).roundToInt()
        val vector = Vector(adjustedX, adjustedY)
        val mouseEvent = MouseEvent.leftButton(vector)

        mouseEventPeer.dispatch(MOUSE_MOVED, MouseEvent.noButton(vector)) // to show tooltip

        when (clickCount) {
            1 -> mouseEventPeer.dispatch(MOUSE_CLICKED, mouseEvent)
            2 -> mouseEventPeer.dispatch(MOUSE_DOUBLE_CLICKED, mouseEvent)
            else -> return
        }
    }
}
