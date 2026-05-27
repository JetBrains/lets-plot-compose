/*
 * Copyright (c) 2023 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package plotSpec

import org.jetbrains.letsPlot.Figure
import org.jetbrains.letsPlot.geom.geomPoint
import org.jetbrains.letsPlot.letsPlot

class IllegalArgumentSpec : PlotDemoFigure {
    override fun createFigure(): Figure {
        val data = mapOf<String, Any>(
            "x" to listOf(0, 1),
            "Y" to listOf(0, 1),
        )

        return letsPlot(data) + geomPoint(size = 5) {
            x = "x"
            y = "y"
            color = "not existent (by design)" // <-- show lead to IllegalArgumentException
        }
    }

    override fun createFigureList(): List<Figure> {

        return listOf(
            createFigure(),
        )
    }
}