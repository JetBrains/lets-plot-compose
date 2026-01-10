package org.jetbrains.letsPlot.compose.canvas

import org.jetbrains.letsPlot.commons.geometry.Vector
import org.jetbrains.letsPlot.commons.registration.Disposable
import org.jetbrains.letsPlot.commons.values.Bitmap
import org.jetbrains.letsPlot.compose.canvas.SkiaUtil.toSkiaImage
import org.jetbrains.letsPlot.core.canvas.Canvas

class SkiaSnapshot(
    val skImage: org.jetbrains.skia.Image
) : Canvas.Snapshot, Disposable {

    override val size: Vector
        get() = Vector(skImage.width, skImage.height)

    override val bitmap: Bitmap by lazy {
        val pixelmap = skImage.peekPixels()
            ?: throw IllegalStateException("SkiaSnapshot: could not get pixels from skImage")

        Bitmap.fromRGBABytes(size.x, size.y, pixelmap.buffer.bytes)
            .also { pixelmap.close() }
    }

    override fun copy(): Canvas.Snapshot {
        return SkiaSnapshot(skImage)
    }

    override fun dispose() {
        skImage.close()
    }

    companion object {
        fun fromBitmap(bitmap: Bitmap): SkiaSnapshot {
            val image = bitmap.toSkiaImage()
            return SkiaSnapshot(image)
        }
    }
}