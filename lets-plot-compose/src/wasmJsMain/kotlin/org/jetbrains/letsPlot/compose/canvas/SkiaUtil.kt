/*
 * Copyright (c) 2026. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

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

        // Use the ByteArray overload: it copies the pixels into the image, so there is no native
        // Data object left alive (the Data-based overload kept a Data referenced by the image that
        // nothing ever closed).
        return org.jetbrains.skia.Image.makeRaster(imageInfo, rgbaBytes(), width * 4)
    }
}
