/*
 * Copyright (c) 2026. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.compose

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.*
import org.jetbrains.letsPlot.commons.event.*
import org.jetbrains.letsPlot.commons.event.MouseEventSpec.*
import org.jetbrains.letsPlot.commons.geometry.Vector
import org.jetbrains.letsPlot.commons.intern.observable.event.EventHandler
import org.jetbrains.letsPlot.commons.registration.Registration
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.time.Clock

class ComposeMouseEventMapper : MouseEventSource, PointerInputEventHandler {
    private val mouseEventPeer = MouseEventPeer()
    private var clickCount: Int = 0
    private var lastClickTime: Long = 0
    private var offsetX: Float = 0f
    private var offsetY: Float = 0f
    private var dragging: Boolean = false
    private var activePointerId: PointerId? = null
    private var downPosition: Offset? = null

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
                val change = activePointerId
                    ?.let { pointerId -> event.changes.firstOrNull { it.id == pointerId } }
                    ?: event.changes.firstOrNull()
                    ?: continue

                val vector = translate(change.position, density)
                val modifiers = extractModifiers(event)
                val mouseEvent = if (change.pressed) {
                    MouseEvent(vector.x, vector.y, Button.LEFT, modifiers)
                } else {
                    MouseEvent(vector.x, vector.y, Button.NONE, modifiers)
                }

                when (event.type) {
                    PointerEventType.Press -> {
                        activePointerId = change.id
                        downPosition = change.position
                        dragging = false

                        val currentTime = Clock.System.now().toEpochMilliseconds()
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
                            dispatchClick(change.position, clickCount, density.toDouble(), modifiers)
                            if (clickCount > 1) {
                                clickCount = 0
                            }
                        }
                        mouseEventPeer.dispatch(MOUSE_RELEASED, mouseEvent)
                        dragging = false
                        activePointerId = null
                        downPosition = null
                    }

                    PointerEventType.Move -> {
                        val movedPastSlop = downPosition?.let { down ->
                            val dx = change.position.x - down.x
                            val dy = change.position.y - down.y
                            dx * dx + dy * dy > viewConfiguration.touchSlop * viewConfiguration.touchSlop
                        } ?: false

                        if (change.pressed && (dragging || movedPastSlop)) {
                            dragging = true
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
                        val scrollAmount = if (abs(scrollDelta.x) > abs(scrollDelta.y)) {
                            scrollDelta.x.toDouble()
                        } else {
                            scrollDelta.y.toDouble()
                        }

                        val wheelMouseEvent = MouseWheelEvent(
                            x = vector.x,
                            y = vector.y,
                            button = Button.NONE,
                            modifiers = modifiers,
                            scrollAmount = scrollAmount
                        )
                        mouseEventPeer.dispatch(MOUSE_WHEEL_ROTATED, wheelMouseEvent)
                    }
                }
            }
        }
    }

    private fun dispatchClick(
        position: Offset,
        clickCount: Int,
        density: Double,
        modifiers: KeyModifiers
    ) {
        val vector = translate(position, density.toFloat())
        val mouseEvent = MouseEvent(vector.x, vector.y, Button.LEFT, modifiers)

        mouseEventPeer.dispatch(MOUSE_MOVED, MouseEvent(vector.x, vector.y, Button.NONE, modifiers))

        when (clickCount) {
            1 -> mouseEventPeer.dispatch(MOUSE_CLICKED, mouseEvent)
            2 -> mouseEventPeer.dispatch(MOUSE_DOUBLE_CLICKED, mouseEvent)
            else -> return
        }
    }

    private fun translate(position: Offset, density: Float): Vector {
        val adjustedX = ((position.x / density) - offsetX).roundToInt()
        val adjustedY = ((position.y / density) - offsetY).roundToInt()
        return Vector(adjustedX, adjustedY)
    }

    private fun extractModifiers(event: PointerEvent): KeyModifiers {
        return KeyModifiers(
            isCtrl = event.keyboardModifiers.isCtrlPressed,
            isAlt = event.keyboardModifiers.isAltPressed,
            isShift = event.keyboardModifiers.isShiftPressed,
            isMeta = event.keyboardModifiers.isMetaPressed
        )
    }
}
