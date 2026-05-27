/*
 * Copyright (c) 2023 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package plotSpec

import demoData.Raster.rasterData_Blue
import demoData.Raster.rasterData_RGB
import org.jetbrains.letsPlot.Figure
import org.jetbrains.letsPlot.geom.geomRaster
import org.jetbrains.letsPlot.letsPlot
import org.jetbrains.letsPlot.scale.scaleFillIdentity

class RasterSpec : PlotDemoFigure {

    override fun createFigureList(): List<Figure> {
        return listOf(
            rasterPlot(rasterData_Blue(), scaleFillIdentity = false),
            rasterPlot(rasterData_RGB(), scaleFillIdentity = true)
        )
    }

    private fun rasterPlot(data: Map<*, *>, scaleFillIdentity: Boolean): Figure {
        var plot = letsPlot(data) +
                geomRaster {
                    x = "x"
                    y = "y"
                    fill = "fill"
                    alpha = "alpha"
                }

        if (scaleFillIdentity) {
            plot += scaleFillIdentity()
        }

        return plot
    }
}