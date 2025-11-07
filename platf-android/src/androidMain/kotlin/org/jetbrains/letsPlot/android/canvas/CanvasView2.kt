/*
 * Copyright (c) 2023 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.android.canvas

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import org.jetbrains.letsPlot.commons.event.MouseEventSource
import org.jetbrains.letsPlot.commons.geometry.DoubleVector
import org.jetbrains.letsPlot.commons.registration.Registration
import org.jetbrains.letsPlot.core.canvasFigure.CanvasFigure2

@SuppressLint("ViewConstructor")
class CanvasView2(
    context: Context,
) : View(context) {
    var onError: (Throwable) -> Unit = { _ -> }
    var figure: CanvasFigure2? = null
        set(fig) {
            if (field == fig) {
                return
            }


            field = fig

            figureRegistration.remove()
            if (fig != null) {
                fig.eventPeer.addEventSource(mouseEventSource)
                figureRegistration = Registration.from(
                    fig.mapToCanvas(canvasPeer),
                    fig.onRepaintRequested { invalidate() }
                )
            }

            requestLayout()
            invalidate()
        }

    private val canvasPeer = AndroidCanvasPeer()

    private var figureRegistration: Registration = Registration.EMPTY
    private val mouseEventSource: MouseEventSource = AndroidMouseEventMapper(this, context) { x, y -> DoubleVector(x - centerOffsetX, y - centerOffsetY) }
    private var centerOffsetX: Float = 0f
    private var centerOffsetY: Float = 0f
    private val eraser = Paint().apply {
        color = Color.TRANSPARENT
        style = Paint.Style.FILL
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val density = resources.displayMetrics.density
        val w = w / density
        val h = h / density

        figure?.resize(w, h)
    }

    override fun onDraw(canvas: android.graphics.Canvas) {
        super.onDraw(canvas)

        val fig = figure ?: return

        val context2d = AndroidContext2d(canvas, resources.displayMetrics.density.toDouble())
        centerOffsetX = ((width - fig.size.x * resources.displayMetrics.density) / 2f)
        centerOffsetY = ((height - fig.size.y * resources.displayMetrics.density) / 2f)

        println("CanvasView.onDraw: view size=($width, $height), figure size=(${fig.size.x}, ${fig.size.y}), offset=($centerOffsetX, $centerOffsetY)")

        context2d.save()
        context2d.translate(centerOffsetX.toDouble() / resources.displayMetrics.density, centerOffsetY.toDouble() / resources.displayMetrics.density)

        fig.paint(context2d)

        context2d.restore()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        figureRegistration.remove()
        canvasPeer.dispose()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val density = resources.displayMetrics.density

        val figureWidthInDp = figure?.size?.x ?: 0
        val figureHeightInDp = figure?.size?.y ?: 0

        val desiredWidth = (figureWidthInDp * density).toInt()
        val desiredHeight = (figureHeightInDp * density).toInt()

        val finalWidth = when (MeasureSpec.getMode(widthMeasureSpec)) {
            MeasureSpec.EXACTLY -> MeasureSpec.getSize(widthMeasureSpec)
            MeasureSpec.AT_MOST -> minOf(desiredWidth, MeasureSpec.getSize(widthMeasureSpec))
            else -> desiredWidth
        }

        val finalHeight = when (MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.EXACTLY -> MeasureSpec.getSize(heightMeasureSpec)
            MeasureSpec.AT_MOST -> minOf(desiredHeight, MeasureSpec.getSize(heightMeasureSpec))
            else -> desiredHeight
        }

        println("finalWidth=$finalWidth, finalHeight=$finalHeight")

        setMeasuredDimension(finalWidth, finalHeight)
    }
}
