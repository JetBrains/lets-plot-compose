/*
 * Copyright (c) 2024 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package plotSpec

import org.jetbrains.letsPlot.Figure
import org.jetbrains.letsPlot.geom.geomSegment
import org.jetbrains.letsPlot.ggplot

class StrokeDashSpec : PlotDemoFigure {
    override fun createFigureList(): List<Figure> {
        return listOf(
            ggplot()
                    + geomSegment(x=0, y=20, xend = 100, yend=20, linetype = listOf(5, listOf(10, 5)))
                    + geomSegment(x=0, y=10, xend = 100, yend=10, linetype = listOf(0, listOf(10, 5)))
                    + geomSegment(x=0, y=0, xend = 100, yend=0),
        )
    }
}