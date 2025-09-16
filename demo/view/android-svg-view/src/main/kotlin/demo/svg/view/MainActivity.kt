/*
 * Copyright (c) 2025 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package demo.svg.view

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import demo.svgModel.ClipPathSvgModel
import demo.svgModel.ReferenceSvgModel
import demo.svgModel.SvgImageElementModel
import org.jetbrains.letsPlot.android.canvas.CanvasView
import org.jetbrains.letsPlot.raster.view.SvgCanvasFigure

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.layoutParams =
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        setContentView(layout, layout.layoutParams)

        layout.addView(Button(this).apply {
            text = "Back"
            setOnClickListener {
                println("Back button clicked")
            }
        })
        // Svg pictures A, B, C
        layout.addView(
            CanvasView(this).apply {
                figure = SvgCanvasFigure(ReferenceSvgModel.createModel())
                setBackgroundColor(Color.GREEN)
            }
        )

        layout.addView(
            CanvasView(this).apply {
                figure = SvgCanvasFigure(SvgImageElementModel.createModel())
                setBackgroundColor(Color.RED)
            }
        )

        layout.addView(
            CanvasView(this).apply {
                figure = SvgCanvasFigure(ClipPathSvgModel.createModel())
                setBackgroundColor(Color.BLUE)
            }
        )
    }
}