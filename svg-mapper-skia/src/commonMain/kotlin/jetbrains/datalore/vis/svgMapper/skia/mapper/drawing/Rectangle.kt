/*
 * Copyright (c) 2022. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.vis.svgMapper.skia.mapper.drawing

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Rect

internal class Rectangle: Figure() {
    var x: Float by visualProp(0.0f)
    var y: Float by visualProp(0.0f)
    var width: Float by visualProp(0.0f)
    var height: Float by visualProp(0.0f)
    private val rect: Rect by dependencyProp(Rectangle::x, Rectangle::y, Rectangle::width, Rectangle::height) {
        Rect.makeXYWH(x, y, width, height)
    }

    override fun doDraw(canvas: Canvas) {
        fillPaint?.let { canvas.drawRect(rect, it) }
        strokePaint?.let { canvas.drawRect(rect, it) }
    }

    override fun doGetBounds(): Rect {
        return Rect.makeXYWH(x, y, width, height).offset(absoluteOffsetX, absoluteOffsetY)
    }
}