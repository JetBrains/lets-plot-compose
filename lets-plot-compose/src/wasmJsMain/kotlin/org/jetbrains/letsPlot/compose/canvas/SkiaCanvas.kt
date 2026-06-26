/*
 * Copyright (c) 2026. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.compose.canvas

import org.jetbrains.letsPlot.commons.geometry.Vector
import org.jetbrains.letsPlot.core.canvas.Canvas
import org.jetbrains.letsPlot.core.canvas.Context2d

class SkiaCanvas private constructor(
    private val skBitmap: org.jetbrains.skia.Bitmap,
    override val size: Vector,
    fontManager: SkiaFontManager
) : Canvas {
    // Free the backing Bitmap when the context is disposed (e.g. by RepaintManager after
    // takeSnapshot). takeSnapshot copies pixels out of the mutable bitmap, so it stays valid.
    override val context2d: Context2d =
        SkiaContext2d(org.jetbrains.skia.Canvas(skBitmap), fontManager, onDispose = { skBitmap.close() })

    override fun takeSnapshot(): Canvas.Snapshot {
        val skImage = org.jetbrains.skia.Image.makeFromBitmap(skBitmap)
        return SkiaSnapshot(skImage)
    }

    companion object {
        fun create(size: Vector, contentScale: Double, fontManager: SkiaFontManager): SkiaCanvas {
            val bitmap = SkiaUtil.createBitmap(size.x, size.y, contentScale)
            return SkiaCanvas(bitmap, size, fontManager)
        }
    }
}