package org.jetbrains.letsPlot.compose.canvas

import org.jetbrains.letsPlot.commons.geometry.Vector
import org.jetbrains.letsPlot.core.canvas.Canvas
import org.jetbrains.letsPlot.core.canvas.Context2d

class SkiaCanvas private constructor(
    private val skBitmap: org.jetbrains.skia.Bitmap,
    override val size: Vector
) : Canvas {
    override val context2d: Context2d = SkiaContext2d(org.jetbrains.skia.Canvas(skBitmap), SkiaFontManager())

    override fun takeSnapshot(): Canvas.Snapshot {
        val skImage = org.jetbrains.skia.Image.makeFromBitmap(skBitmap)
        return SkiaSnapshot(skImage)
    }

    companion object {
        fun create(size: Vector, contentScale: Double): SkiaCanvas {
            val bitmap = SkiaUtil.createBitmap(size.x, size.y, contentScale)
            return SkiaCanvas(bitmap, size)
        }
    }
}