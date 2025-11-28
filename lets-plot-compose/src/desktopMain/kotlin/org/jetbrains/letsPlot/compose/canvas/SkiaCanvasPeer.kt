package org.jetbrains.letsPlot.compose.canvas

import org.jetbrains.letsPlot.commons.geometry.Vector
import org.jetbrains.letsPlot.commons.intern.async.Async
import org.jetbrains.letsPlot.commons.values.Bitmap
import org.jetbrains.letsPlot.core.canvas.Canvas
import org.jetbrains.letsPlot.core.canvas.CanvasPeer
import org.jetbrains.letsPlot.core.canvas.Font
import org.jetbrains.letsPlot.core.canvas.TextMetrics

class SkiaCanvasPeer : CanvasPeer {
    private val measureCanvas = SkiaCanvas.create(1, 1)

    override fun createCanvas(size: Vector): Canvas {
        return SkiaCanvas.create(size.x, size.y)
    }

    override fun createSnapshot(bitmap: Bitmap): Canvas.Snapshot {
        return SkiaSnapshot(bitmap)
    }

    override fun decodeDataImageUrl(dataUrl: String): Async<Canvas.Snapshot> {
        TODO("Not yet implemented")
    }

    override fun decodePng(png: ByteArray): Async<Canvas.Snapshot> {
        TODO("Not yet implemented")
    }

    override fun dispose() {
        TODO("Not yet implemented")
    }

    override fun measureText(text: String, font: Font): TextMetrics {
        val ctx = measureCanvas.context2d
        ctx.save()
        ctx.setFont(font)
        val textMetrics = ctx.measureText(text)
        ctx.restore()

        return textMetrics
    }
}