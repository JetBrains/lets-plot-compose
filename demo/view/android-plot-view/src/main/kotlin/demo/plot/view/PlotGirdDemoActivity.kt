/*
 * Copyright (c) 2025 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package demo.plot.view

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import org.jetbrains.letsPlot.android.canvas.CanvasView2
import org.jetbrains.letsPlot.core.util.MonolithicCommon
import org.jetbrains.letsPlot.core.util.sizing.SizingPolicy
import org.jetbrains.letsPlot.intern.toSpec
import org.jetbrains.letsPlot.raster.view.PlotCanvasFigure2
import plotSpec.PlotGridSpec

class PlotGirdDemoActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val plotFigure = PlotGridSpec().createFigure()

        setContentView(
            CanvasView2(this).apply {
                figure = PlotCanvasFigure2().apply {
                    update(
                        processedSpec = MonolithicCommon.processRawSpecs(plotFigure.toSpec(), frontendOnly = false),
                        sizingPolicy = SizingPolicy.fitContainerSize(false),
                        computationMessagesHandler = {}
                    )
                }

                setBackgroundColor(Color.GREEN)
            }
        )
    }
}