/*
 * Copyright (c) 2023 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package plotSpec

import demoData.Iris
import org.jetbrains.letsPlot.Figure
import org.jetbrains.letsPlot.geom.geomDensity
import org.jetbrains.letsPlot.geom.geomPoint
import org.jetbrains.letsPlot.gggrid
import org.jetbrains.letsPlot.intern.figure.SubPlotsFigure
import org.jetbrains.letsPlot.label.ggtitle
import org.jetbrains.letsPlot.letsPlot
import org.jetbrains.letsPlot.scale.scaleYContinuous
import org.jetbrains.letsPlot.themes.themeBW

class PlotGridSpec : PlotDemoFigure {

    override fun createFigure(): Figure {
        return irisTriple_compositeCell(innerAlignment = true)
    }

    override fun createFigureList(): List<Figure> {
        return listOf(
            irisTriple(innerAlignment = false),
            irisTriple(innerAlignment = true),
            irisTriple_compositeCell(innerAlignment = false),
            irisTriple_compositeCell(innerAlignment = true),
        )
    }

    private fun irisTriple(
        colWidths: List<Double>? = null,
        rowHeights: List<Double>? = null,
        innerAlignment: Boolean
    ): SubPlotsFigure {

        val scatterSpec = irisScatterPlot()
        val densitySpec = irisDensityPlot()

        return gggrid(
            listOf(
                densitySpec, null,
                scatterSpec, densitySpec
            ),
            ncol = 2,
            widths = colWidths,
            heights = rowHeights,
            align = innerAlignment
        )
    }

    private fun irisScatterPlot(): Figure {
        return letsPlot(Iris.map()) {
            x = "sepal length (cm)"
            y = "sepal width (cm)"
        } + geomPoint(size = 5, color = "black", alpha = 0.4) +
                themeBW() + ggtitle("Bottom-Left")
    }

    private fun irisDensityPlot(): Figure {
        return letsPlot(Iris.map()) {
            x = "sepal length (cm)"
        } + geomDensity(size = 1.5, color = "black", fill = "black", alpha = 0.1) +
                themeBW() +
                scaleYContinuous(position = "right")
    }

    @Suppress("FunctionName")
    private fun irisTriple_compositeCell(
        colWidths: List<Double>? = null,
        rowHeights: List<Double>? = null,
        innerAlignment: Boolean
    ): Figure {
        val scatterSpec = irisScatterPlot()
        val densitySpec = irisDensityPlot()

        val innerSubplots = gggrid(
            listOf(scatterSpec, densitySpec),
            ncol = 2,
            align = false
        )

        return gggrid(
            listOf(
                densitySpec, innerSubplots,
            ),
            ncol = 1,
            widths = colWidths,
            heights = rowHeights,
            align = innerAlignment
        )
    }
}