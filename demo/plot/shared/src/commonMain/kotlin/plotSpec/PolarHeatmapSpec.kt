/*
 * Copyright (c) 2024 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package plotSpec

import org.jetbrains.letsPlot.Figure
import org.jetbrains.letsPlot.coord.coordPolar
import org.jetbrains.letsPlot.geom.geomTile
import org.jetbrains.letsPlot.ggplot
import org.jetbrains.letsPlot.label.ggtitle
import org.jetbrains.letsPlot.scale.scaleBrewer
import org.jetbrains.letsPlot.themes.elementBlank
import org.jetbrains.letsPlot.themes.theme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class PolarHeatmapSpec : PlotDemoFigure {
    override fun createFigureList(): List<Figure> {
        return listOf(
            polarHeatmap()
        )
    }

    private fun polarHeatmap(): Figure {
        val maxR = 100
        val stepsR = 4 * maxR
        val maxTheta = 2 * PI
        val stepsTheta = 200
        val dataFunction: (Double, Double) -> Double = { x, y -> sin(x * 7) + cos(y / 11) }

        fun linspace(start: Double, stop: Double, num: Int): List<Double> {
            if (num <= 0) return emptyList()
            if (num == 1) return listOf(start)
            val step = (stop - start) / (num - 1)
            return List(num) { start + it * step }
        }

        fun simpleMeshgrid(xs: List<Double>, ys: List<Double>): Pair<List<List<Double>>, List<List<Double>>> {
            return Pair(
                List(ys.size) { xs },
                ys.map { y -> List(xs.size) { y } }
            )
        }

        fun getData(xs: List<Double>, ys: List<Double>, f: (Double, Double) -> Double): Map<String, List<Double>> {
            val zs = (xs zip ys).map { p -> f(p.first, p.second) }
            return mapOf("x" to xs, "y" to ys, "z" to zs)
        }

        val (gridR, gridTheta) = simpleMeshgrid(
            linspace(0.0, maxR.toDouble(), stepsR),
            linspace(0.0, maxTheta.toDouble(), stepsTheta)
        )

        val dataMap = getData(gridTheta.flatten(), gridR.flatten(), dataFunction)

        val p = ggplot(dataMap) +
                geomTile(size = 1) { x = "x"; y = "y"; color = "z"; fill = "z" } +
                scaleBrewer(listOf("color", "fill"), palette = "Spectral", direction = -1) +
                theme(axisTitle = elementBlank())

        return p + coordPolar() + ggtitle("Cartesian Heatmap")
    }
}