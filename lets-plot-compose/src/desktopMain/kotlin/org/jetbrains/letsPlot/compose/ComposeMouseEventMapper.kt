package org.jetbrains.letsPlot.compose

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerInputEventHandler
import androidx.compose.ui.input.pointer.PointerInputScope
import org.jetbrains.letsPlot.commons.event.*
import org.jetbrains.letsPlot.commons.event.MouseEventSpec.*
import org.jetbrains.letsPlot.commons.geometry.Vector
import org.jetbrains.letsPlot.commons.intern.observable.event.EventHandler
import org.jetbrains.letsPlot.commons.registration.Registration
import kotlin.math.roundToInt

class ComposeMouseEventMapper : MouseEventSource, PointerInputEventHandler {
    private val mouseEventPeer = MouseEventPeer()
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
                        if (clickCount > 0) {
                            val pos = event.changes.first().position
                            dispatchClick(pos, clickCount, density.toDouble())
                            if (clickCount > 1) {
                                clickCount = 0 // Reset after a double click
                            }
                        }
                        mouseEventPeer.dispatch(MOUSE_RELEASED, mouseEvent)
                    }

                    PointerEventType.Move -> {
                        if (change.pressed) {
                            mouseEventPeer.dispatch(MOUSE_DRAGGED, mouseEvent)
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

        when (clickCount) {
            1 -> mouseEventPeer.dispatch(MOUSE_CLICKED, mouseEvent)
            2 -> mouseEventPeer.dispatch(MOUSE_DOUBLE_CLICKED, mouseEvent)
            else -> return
        }
    }

}