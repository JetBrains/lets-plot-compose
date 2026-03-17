/*
 * Copyright (c) 2024 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package plotSpec

import org.jetbrains.letsPlot.Figure
import org.jetbrains.letsPlot.geom.extras.arrow
import org.jetbrains.letsPlot.geom.geomCurve
import org.jetbrains.letsPlot.intern.Plot
import org.jetbrains.letsPlot.label.ggtitle
import org.jetbrains.letsPlot.letsPlot
import org.jetbrains.letsPlot.scale.xlim

class CurveSpec : PlotDemoFigure {

    override fun createFigureList(): List<Figure> {

        fun curvePlot(curvature: Double, angle: Int, ncp: Int = 0): Plot {
            val data = mapOf(
                "x" to listOf(-10),
                "y" to listOf(1),
                "xend" to listOf(10),
                "yend" to listOf(-1),
            )

            return letsPlot(data) { x = "x"; y = "y"; xend = "xend"; yend = "yend" } +
                    geomCurve(curvature = curvature, angle = angle, ncp = ncp, arrow = arrow(ends = "both")) +
                    xlim(listOf(-15, 15)) +
                    ggtitle("curvature = $curvature, angle=$angle, ncp=$ncp")
        }

        return listOf(
            curvePlot(curvature = 0.5, angle = 0, ncp = 5),
            curvePlot(curvature = 0.5, angle = 90, ncp = 1),
            curvePlot(curvature = 0.5, angle = 45, ncp = 5),
            curvePlot(curvature = -1.0, angle = 45, ncp = 5),
            curvePlot(curvature = 0.7, angle = 30, ncp = 5),
            curvePlot(curvature = -0.7, angle = 30, ncp = 5),
        )
    }
}
