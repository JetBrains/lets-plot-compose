/*
 * Copyright (c) 2024 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package plotSpec

import org.jetbrains.letsPlot.Figure
import org.jetbrains.letsPlot.geom.geomPath
import org.jetbrains.letsPlot.label.ggtitle
import org.jetbrains.letsPlot.letsPlot

class VariadicPathSpec : PlotDemoFigure {
    override fun createFigureList(): List<Figure> {
        return listOf(
            variadicPathPlot()
        )
    }

    private fun variadicPathPlot(): Figure {
        val data = mapOf<String, Any>(
            "x" to listOf(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0 ),
            "y" to listOf(0.0, 5.0, 0.0, 10.0, 0.0, 5.0, 0.0, 5.0, 0.0 ),
            "g" to listOf(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, 2.0, 2.0, 2.0 ),
            "c" to listOf(1.0, 17.0, 4.0, 8.0, 3.0, 15.0, 15.0, 2.0, 9.0 ),
            "s" to listOf(10.0, 10.0, 10.0, 8.0, 3.0, 9.0, 15.0, 12.0, 9.0),
        )
        return letsPlot(data) + geomPath() { x = "x"; y = "y"; size = "s"; color = "c" } + ggtitle("Variadic Path")
    }
}