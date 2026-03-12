package org.jetbrains.letsPlot.android.canvas

import org.jetbrains.letsPlot.commons.geometry.Vector
import org.jetbrains.letsPlot.commons.values.Bitmap
import org.jetbrains.letsPlot.core.canvas.Canvas

class AndroidSnapshot(
    val platformBitmap: android.graphics.Bitmap
) : Canvas.Snapshot {
    override val bitmap: Bitmap
        get() {
            val argbInts = IntArray(platformBitmap.width * platformBitmap.height)
            platformBitmap.getPixels(argbInts, 0, platformBitmap.width, 0, 0, platformBitmap.width, platformBitmap.height)
            return Bitmap(platformBitmap.width, platformBitmap.height, argbInts)
        }

    override val size: Vector
        get() = Vector(platformBitmap.width, platformBitmap.height)

    override fun copy(): Canvas.Snapshot {
        val newBitmap = platformBitmap.copy(android.graphics.Bitmap.Config.ARGB_8888, false)
        return AndroidSnapshot(newBitmap)
    }

    override fun dispose() {
        platformBitmap.recycle()
    }

    companion object {
        fun fromBitmap(bitmap: Bitmap): AndroidSnapshot {
            val platformBitmap = android.graphics.Bitmap.createBitmap(bitmap.width, bitmap.height, android.graphics.Bitmap.Config.ARGB_8888)
            platformBitmap.setPixels(bitmap.argbInts, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            return AndroidSnapshot(platformBitmap)
        }
    }
}