/*
 * Copyright (c) 2025 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package demo.svg.view

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import demo.svgModel.ReferenceSvgModel
import org.jetbrains.letsPlot.android.canvas.CanvasView2
import org.jetbrains.letsPlot.raster.view.SvgCanvasFigure

class SvgReferenceWrapContent : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(
            CanvasView2(this).apply {
                figure = SvgCanvasFigure(ReferenceSvgModel.createModel())
                setBackgroundColor(Color.BLUE)
            },
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        )
    }
}