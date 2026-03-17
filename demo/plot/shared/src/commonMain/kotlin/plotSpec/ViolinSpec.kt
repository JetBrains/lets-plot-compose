/*
 * Copyright (c) 2023 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package plotSpec

import demoData.Iris
import org.jetbrains.letsPlot.Figure
import org.jetbrains.letsPlot.geom.geomViolin
import org.jetbrains.letsPlot.label.ggtitle
import org.jetbrains.letsPlot.label.labs
import org.jetbrains.letsPlot.letsPlot
import org.jetbrains.letsPlot.scale.scaleColorGradient
import org.jetbrains.letsPlot.themes.elementText
import org.jetbrains.letsPlot.themes.theme
import kotlin.Double.Companion.NaN

class ViolinSpec : PlotDemoFigure {

    override fun createFigureList(): List<Figure> {
        return listOf(
            basic(),
            withNan(),
            withGroups(),
            halfViolins()
        )
    }

    private fun basic(): Figure {
        return letsPlot(Iris.map()) {
            x = "target"
            y = "sepal length (cm)"
            fill = "target"
        } + geomViolin(
            alpha = 0.7,
            quantiles = listOf(0, 0.1, 0.5, 0.9, 1),
            quantileLines = true
        ) + ggtitle("Basic")
    }

    private fun withNan(): Figure {
        return letsPlot(
            data = mapOf(
                "class" to listOf(0, 0, 0, null, 1, 1, 1, 1),
                "value" to listOf(0, 0, 2, 2, 1, 1, 3, NaN)
            )
        ) {
            x = "class"
            y = "value"
        } + geomViolin() +
                ggtitle("Violin", "NaNs in data") +
                theme(
                    title = elementText(family = "Verdana", face = "bold_italic"),
                )
    }

    private fun withGroups(): Figure {
        return letsPlot(
            mapOf(
                "class" to listOf('A', 'A', 'A', 'A', 'A', 'A', 'B', 'B', 'B', 'B', 'B', 'B'),
                "group" to listOf('x', 'x', 'x', 'y', 'y', 'y', 'x', 'x', 'x', 'x', 'y', 'y'),
                "value" to listOf(0, 0, 2, 1, 1, 3, 1, 3, 3, 5, 2, 4)
            )
        ) {
            x = "class"
            y = "value"
            fill = "group"
        } + geomViolin(quantileLines = true) +
                labs(title = "Violin", caption = "Additional grouping") +
                theme(title = elementText(family = "Courier", size = 18))
    }

    private fun halfViolins(): Figure {
        return letsPlot(Iris.map()) {
            x = "target"
            y = "sepal length (cm)"
            fill = "target"
        } + geomViolin(
            quantileLines = true,
            showHalf = -1,
            trim = false
        ) + geomViolin(
            quantiles = listOf(0.1, 0.5, 0.9),
            showHalf = 1,
            trim = false,
            fill = "#ffffb2"
        ) {
            color = "..quantile.."
        } + scaleColorGradient(
            low = "#d73027",
            high = "#1a9850"
        )
    }
}