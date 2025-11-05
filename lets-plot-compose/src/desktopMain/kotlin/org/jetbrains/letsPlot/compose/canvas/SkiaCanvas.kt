package org.jetbrains.letsPlot.compose.canvas

import androidx.compose.ui.graphics.nativeCanvas
import org.jetbrains.letsPlot.commons.geometry.Vector
import org.jetbrains.letsPlot.core.canvas.Canvas
import org.jetbrains.letsPlot.core.canvas.Context2d

class SkiaCanvas(
    private val img: androidx.compose.ui.graphics.ImageBitmap
) : Canvas {
    override val context2d: Context2d = SkiaContext2d(androidx.compose.ui.graphics.Canvas(img).nativeCanvas, SkiaFontManager())
    override val size: Vector
        get() = TODO("Not yet implemented")

    override fun takeSnapshot(): Canvas.Snapshot {
        TODO("Not yet implemented")
    }

    companion object {
        fun create(width: Number, height: Number): SkiaCanvas {
            val bitmap = androidx.compose.ui.graphics.ImageBitmap(width.toInt(), height.toInt())
            return SkiaCanvas(bitmap)
        }
    }
}