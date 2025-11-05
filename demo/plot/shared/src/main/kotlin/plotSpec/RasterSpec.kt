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
import kotlin.random.Random

class RasterSpec : PlotDemoFigure {

    override fun createFigureList(): List<Figure> {
        return listOf(
            rasterPlot(rasterData_Blue(), scaleFillIdentity = false),
            rasterPlot(rasterData_RGB(), scaleFillIdentity = true)
        )
    }

    fun issue46(): Figure {
        val rand = Random(12)
        val n = 3

        val data = mapOf(
            "x" to List(n) { rand.nextDouble() },
            "y" to List(n) { rand.nextDouble() },
            "col" to List(n) { rand.nextDouble() },
        )


        // geomRaster with n = 3 shows orange/brown squares
        return letsPlot(data) + geomRaster {
            x = "x"
            y = "y"
            fill = "col"
        }
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