package org.jetbrains.letsPlot.android.canvas

import org.jetbrains.letsPlot.commons.geometry.Vector
import org.jetbrains.letsPlot.commons.intern.async.Async
import org.jetbrains.letsPlot.commons.values.Bitmap
import org.jetbrains.letsPlot.core.canvas.Canvas
import org.jetbrains.letsPlot.core.canvas.CanvasPeer

class AndroidCanvasPeer(
    val pixelDensity: Double = 1.0
) : CanvasPeer {
    private val measureCanvas = AndroidCanvas.create(Vector(1, 1), pixelDensity)

    override fun createCanvas(size: Vector): AndroidCanvas {
        return AndroidCanvas.create(size, pixelDensity)
    }

    override fun createCanvas(
        size: Vector,
        contentScale: Double
    ): Canvas {
        return AndroidCanvas.create(size, contentScale)
    }

    override fun createSnapshot(bitmap: Bitmap): AndroidSnapshot {
        return AndroidSnapshot.fromBitmap(bitmap)
    }

    override fun decodeDataImageUrl(dataUrl: String): Async<Canvas.Snapshot> {
        error("decodeDataImageUrl not supported in AndroidCanvasControl")
    }

    override fun decodePng(png: ByteArray): Async<Canvas.Snapshot> {
        error("decodePng not supported in AndroidCanvasControl")
    }
}