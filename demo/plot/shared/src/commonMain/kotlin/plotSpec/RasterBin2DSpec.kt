/*
 * Copyright (c) 2023 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package plotSpec

import org.jetbrains.letsPlot.Figure
import org.jetbrains.letsPlot.Stat
import org.jetbrains.letsPlot.geom.geomRaster
import org.jetbrains.letsPlot.intern.Plot
import org.jetbrains.letsPlot.letsPlot
import org.jetbrains.letsPlot.sampling.samplingNone
import kotlin.random.Random

class RasterBin2DSpec : PlotDemoFigure {

    @Suppress("DuplicatedCode")
    private fun plot(): Plot {
        val rand = Random(12)
        val n = 25_000

        val data = mapOf(
            "x" to List(n) { rand.nextDouble() },
            "y" to List(n) { rand.nextDouble() },
            "col" to List(n) { rand.nextDouble() },
        )

        return letsPlot(data) + geomRaster(stat = Stat.bin2D(), sampling = samplingNone) {
            x = "x"
            y = "y"
            fill = "col"
        }
    }

    override fun createFigureList(): List<Figure> {
        return listOf(plot())
    }
}