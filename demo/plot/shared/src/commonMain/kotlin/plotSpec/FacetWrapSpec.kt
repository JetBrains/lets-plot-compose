/*
 * Copyright (c) 2023 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package plotSpec

import demoData.AutoMpg
import org.jetbrains.letsPlot.Figure
import org.jetbrains.letsPlot.facet.facetWrap
import org.jetbrains.letsPlot.geom.geomPoint
import org.jetbrains.letsPlot.intern.Plot
import org.jetbrains.letsPlot.letsPlot
import org.jetbrains.letsPlot.themes.themeGrey

class FacetWrapSpec : PlotDemoFigure {

    override fun createFigureList(): List<Figure> {
        val oneFacetDef = commonSpecs() + facetWrap(facets = "number of cylinders", format = "{d} cyl")
        val oneFacet3cols = commonSpecs() + facetWrap(facets = "number of cylinders", ncol = 3, format = "{d} cyl")
        val oneFacet4rows =
            commonSpecs() + facetWrap(facets = "number of cylinders", ncol = 4, dir = "v", format = "{d} cyl")
        val twoFacets = commonSpecs() + facetWrap(
            facets = listOf("origin of car", "number of cylinders"),
            ncol = 5,
            format = listOf(null, "{d} cyl")
        )
        val twoFacetsCylindersOrderDesc = commonSpecs() + facetWrap(
            facets = listOf("origin of car", "number of cylinders"),
            ncol = 5,
            order = listOf(null, -1),
            format = listOf(null, "{d} cyl")
        )

        return listOf(
            oneFacetDef,
            oneFacet3cols,
            oneFacet4rows,
            twoFacets,
            twoFacetsCylindersOrderDesc,
        )
    }

    private fun commonSpecs(): Plot {
        return letsPlot(AutoMpg.map()) {
            x = "engine horsepower"
            y = "miles per gallon"
            color = "origin of car"
        } + geomPoint() + themeGrey()
    }
}