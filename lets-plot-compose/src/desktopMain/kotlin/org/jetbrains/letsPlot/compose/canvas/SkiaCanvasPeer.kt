package org.jetbrains.letsPlot.compose.canvas

import org.jetbrains.letsPlot.commons.geometry.Vector
import org.jetbrains.letsPlot.commons.intern.async.Async
import org.jetbrains.letsPlot.commons.values.Bitmap
import org.jetbrains.letsPlot.core.canvas.Canvas
import org.jetbrains.letsPlot.core.canvas.CanvasPeer

class SkiaCanvasPeer : CanvasPeer {
    override fun createCanvas(size: Vector): Canvas {
        return SkiaCanvas.create(size, contentScale = 1.0)
    }

    override fun createCanvas(size: Vector, contentScale: Double): Canvas {
        return SkiaCanvas.create(size, contentScale)
    }

    override fun createSnapshot(bitmap: Bitmap): SkiaSnapshot {
        return SkiaSnapshot.fromBitmap(bitmap)
    }

    override fun decodeDataImageUrl(dataUrl: String): Async<Canvas.Snapshot> {
        TODO("Not yet implemented")
    }

    override fun decodePng(png: ByteArray): Async<Canvas.Snapshot> {
        TODO("Not yet implemented")
    }
}