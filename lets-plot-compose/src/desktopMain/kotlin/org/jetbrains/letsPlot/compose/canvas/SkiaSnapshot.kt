package org.jetbrains.letsPlot.compose.canvas

import org.jetbrains.letsPlot.commons.geometry.Vector
import org.jetbrains.letsPlot.commons.registration.Disposable
import org.jetbrains.letsPlot.commons.values.Bitmap
import org.jetbrains.letsPlot.core.canvas.Canvas
import org.jetbrains.skia.Image

class SkiaSnapshot(
    override val bitmap: Bitmap
) : Canvas.Snapshot, Disposable {
    val skImage: Image = Image.makeRaster(
        org.jetbrains.skia.ImageInfo(
            width = bitmap.width,
            height = bitmap.height,
            colorType = org.jetbrains.skia.ColorType.RGBA_8888,
            alphaType = org.jetbrains.skia.ColorAlphaType.PREMUL
        ),
        bitmap.rgbaBytes(),
        bitmap.width * 4
    )

    override val size: Vector
        get() = Vector(bitmap.width, bitmap.height)

    override fun copy(): Canvas.Snapshot {
        error("SkiaSnapshot could not be copy")
    }

    override fun dispose() {
        skImage.close()
    }
}