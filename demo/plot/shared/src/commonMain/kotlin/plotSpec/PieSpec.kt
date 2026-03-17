/*
 * Copyright (c) 2023 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package plotSpec

import org.jetbrains.letsPlot.Figure
import org.jetbrains.letsPlot.Stat
import org.jetbrains.letsPlot.geom.geomPie
import org.jetbrains.letsPlot.letsPlot

class PieSpec : PlotDemoFigure {
    override fun createFigureList(): List<Figure> {
        return listOf(
            simplePie(),
            multiPie()
        )
    }

    private fun multiPie(): Figure {

        val data = mapOf(
            "x" to listOf("a", "a", "a", "a", "a", "b", "b", "c", "c", "c"),
            "y" to listOf(1, 1, 1, 1, 1, 2, 2, 1.5, 1.5, 1.5),
            "s" to listOf(3, 1, 2, 1, 4, 1, 3, 3, 3, 1),
            "n" to listOf("a", "b", "a", "c", "a", "a", "b", "c", "a", "b")
        )
        return letsPlot(data) +
                geomPie(size = 10, hole=0.3) {
                    x = "x"
                    y = "y"
                    slice = "s"
                    fill = "n"
                }
    }

    private fun simplePie(): Figure {
        val data = mapOf(
            "name" to listOf('a', 'b', 'c', 'd', 'b'),
            "value" to listOf(40, 90, 10, 50, 20)
        )
        return letsPlot(data) +
                geomPie(stat = Stat.identity, size = 0.7, sizeUnit = "x") {
                    slice = "value"
                    fill = "name"
                }
    }
}
