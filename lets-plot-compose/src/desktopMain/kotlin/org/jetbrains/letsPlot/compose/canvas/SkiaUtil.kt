package org.jetbrains.letsPlot.compose.canvas

import org.jetbrains.letsPlot.commons.values.Bitmap

object SkiaUtil {
    val COLOR_INFO = org.jetbrains.skia.ColorInfo(
        org.jetbrains.skia.ColorType.RGBA_8888,
        org.jetbrains.skia.ColorAlphaType.UNPREMUL,
        org.jetbrains.skia.ColorSpace.sRGB,
    )

    fun createBitmap(width: Int, height: Int, contentScale: Double): org.jetbrains.skia.Bitmap {
        val bitmap = org.jetbrains.skia.Bitmap()
        bitmap.allocPixels(
            imageInfo = org.jetbrains.skia.ImageInfo(
                colorInfo = COLOR_INFO,
                width = (width * contentScale).toInt(),
                height = (height * contentScale).toInt()
            )
        )

        return bitmap
    }

    fun Bitmap.toSkiaImage(): org.jetbrains.skia.Image {
        val imageInfo = org.jetbrains.skia.ImageInfo(
            colorInfo = COLOR_INFO,
            width = width,
            height = height
        )

        val data = org.jetbrains.skia.Data.makeFromBytes(rgbaBytes())
        val image = org.jetbrains.skia.Image.makeRaster(imageInfo, data, width * 4)
        return image
    }
}
