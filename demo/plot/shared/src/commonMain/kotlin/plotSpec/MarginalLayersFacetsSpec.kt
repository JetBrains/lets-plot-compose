/*
 * Copyright (c) 2023 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package plotSpec

import demoData.AutoMpg
import org.jetbrains.letsPlot.Figure
import org.jetbrains.letsPlot.coord.coordFixed
import org.jetbrains.letsPlot.facet.facetGrid
import org.jetbrains.letsPlot.facet.facetWrap
import org.jetbrains.letsPlot.geom.geomDensity
import org.jetbrains.letsPlot.geom.geomHistogram
import org.jetbrains.letsPlot.geom.geomPoint
import org.jetbrains.letsPlot.ggmarginal
import org.jetbrains.letsPlot.intern.Feature
import org.jetbrains.letsPlot.intern.Plot
import org.jetbrains.letsPlot.label.ggtitle
import org.jetbrains.letsPlot.letsPlot
import org.jetbrains.letsPlot.themes.themeGrey

class MarginalLayersFacetsSpec : PlotDemoFigure {

    override fun createFigureList(): List<Figure> {
        val grid = commonSpecs("Grid") + facetGrid(y = "origin of car")
        val gridCoordFixed = commonSpecs("Grid, coord fixed") + facetGrid(y = "origin of car") + coordFixed()
        val gridFreeY = commonSpecs("Grid, scales free_y") + facetGrid(y = "origin of car", scales = "free_y")
        val wrap = commonSpecs("Wrap") + facetWrap(facets = "number of cylinders", format = "{d} cyl")
        val wrapCoordFixed = commonSpecs("Wrap, coord fixed") +
                facetWrap(facets = "number of cylinders", format = "{d} cyl") +
                coordFixed()
        val wrapFreeX = commonSpecs("Wrap, scales free_x") +
                facetWrap(facets = "number of cylinders", format = "{d} cyl", scales = "free_x")


        return listOf(
            grid,
            gridCoordFixed,
            gridFreeY,
            wrap,
            wrapCoordFixed,
            wrapFreeX,
        )
    }

    private fun commonSpecs(title: String): Plot {
        var plot = letsPlot(AutoMpg.map()) {
            x = "engine horsepower"
            y = "miles per gallon"
            color = "origin of car"
        } + geomPoint() + themeGrey() + ggtitle(title)

        val sides = listOf("l", "t", "r", "b")
        val sizes = listOf(0.1, 0.1, 0.2, 0.2)
        for ((side, size) in sides.zip(sizes)) {
            plot += marginalHist(side, size)
            plot += marginalDensity(side, size)
        }

        return plot
    }

    private fun marginalHist(side: String, size: Double): Feature {
        val orientation = when (side) {
            "l", "r" -> "y"
            else -> "x"
        }
        val aesY = when (orientation) {
            "x" -> "y"
            else -> "x"
        }

        return ggmarginal(
            sides = side,
            size = size,
            layer = geomHistogram(bins = 10, color = "white", orientation = orientation) {
                when (aesY) {
                    "y" -> y = "..density.."
                    else -> x = "..density.."
                }
                fill = "origin of car"
            }
        )
    }

    private fun marginalDensity(side: String, size: Double): Feature {
        val orientation = when (side) {
            "l", "r" -> "y"
            else -> "x"
        }

        return ggmarginal(
            sides = side,
            size = size,
            layer = geomDensity(alpha = 0.1, color = "red", fill = "blue", orientation = orientation)
        )
    }
}