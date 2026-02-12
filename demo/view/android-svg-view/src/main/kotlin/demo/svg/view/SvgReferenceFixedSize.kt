/*
 * Copyright (c) 2025 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package demo.svg.view

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import demo.svgModel.ReferenceSvgModel
import org.jetbrains.letsPlot.android.canvas.CanvasView2
import org.jetbrains.letsPlot.raster.view.SvgCanvasDrawable

class SvgReferenceFixedSize : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(
            CanvasView2(this).apply {
                canvasDrawable = SvgCanvasDrawable(ReferenceSvgModel.createModel())
                setBackgroundColor(Color.BLUE)
            },
            ViewGroup.LayoutParams(500, 500)
        )
    }
}