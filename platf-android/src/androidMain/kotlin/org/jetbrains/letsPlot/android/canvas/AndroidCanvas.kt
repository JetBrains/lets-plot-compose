/*
 * Copyright (c) 2025 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.android.canvas

import android.graphics.Bitmap
import org.jetbrains.letsPlot.commons.geometry.Vector
import org.jetbrains.letsPlot.core.canvas.Canvas
import org.jetbrains.letsPlot.core.canvas.Context2d
import kotlin.math.roundToInt

class AndroidCanvas(
    val platformBitmap: Bitmap,
    override val size: Vector,
    pixelDensity: Double
) : Canvas {
    companion object {
        fun create(size: Vector, pixelDensity: Double): AndroidCanvas {
            val w = (size.x * pixelDensity).roundToInt().coerceAtLeast(1)
            val h = (size.y * pixelDensity).roundToInt().coerceAtLeast(1)

            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

            return AndroidCanvas(bitmap, size, pixelDensity)
        }
    }

    override val context2d: Context2d = AndroidContext2d(platformBitmap, pixelDensity)

    override fun takeSnapshot(): AndroidSnapshot {
        return AndroidSnapshot(platformBitmap.copy(Bitmap.Config.ARGB_8888, false))
    }

}
