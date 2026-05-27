/*
 * Copyright (c) 2024 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package plotSpec

import org.jetbrains.letsPlot.Figure
import org.jetbrains.letsPlot.geom.geomLabel
import org.jetbrains.letsPlot.letsPlot

class LabelSpec : PlotDemoFigure {
    override fun createFigureList(): List<Figure> {
        val fonts = run {
            val families = listOf(
                "Arial",
                "Calibri",
                "Garamond",
                "Geneva",
                "Georgia",
                "Helvetica",
                "Lucida Grande",
                "Rockwell",
                "Times New Roman",
                "Verdana",
                "sans-serif",
                "serif",
                "monospace"
            )
            letsPlot() + geomLabel {
                y = families.indices
                label = families
                family = families
            }
        }

        return listOf(fonts)
    }
}