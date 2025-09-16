/*
 * Copyright (c) 2025 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package demo.plot.view

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import demo.plot.view.ResizingWithFixedPlotSizePolicyDemoActivity.Companion.setupResizableCanvas
import org.jetbrains.letsPlot.android.canvas.CanvasView
import org.jetbrains.letsPlot.core.util.sizing.SizingPolicy
import org.jetbrains.letsPlot.intern.toSpec
import org.jetbrains.letsPlot.raster.builder.MonolithicCanvas
import plotSpec.DensitySpec

class ResizingWithFitContainerSizePolicyDemoActivity: Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val plotFigure = DensitySpec().createFigure()

        val view = CanvasView(this).apply {
            figure = MonolithicCanvas.buildPlotFigureFromRawSpec(
                rawSpec = plotFigure.toSpec(),
                sizingPolicy = SizingPolicy.fitContainerSize(false),
                computationMessagesHandler = {}
            )
            setBackgroundColor(Color.GREEN)
        }



        setupResizableCanvas(view)
    }
}