/*
 * Copyright (c) 2024 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package plotSpec

import demoData.AutoMpg
import org.jetbrains.letsPlot.Figure
import org.jetbrains.letsPlot.geom.geomLabel
import org.jetbrains.letsPlot.geom.geomPoint
import org.jetbrains.letsPlot.intern.Plot
import org.jetbrains.letsPlot.letsPlot

class AutoSpec : PlotDemoFigure {
    override fun createFigureList(): List<Figure> {
        return listOf(
            scatter()
        )
    }

    fun scatter(): Plot {
        return letsPlot(AutoMpg.map()) + geomPoint {
            x = "engine horsepower"
            y = "miles per gallon"
            color = "origin of car"
        } + geomLabel(
            checkOverlap = true,
        ) {
            x = "engine horsepower"
            y = "miles per gallon"
            color = "origin of car"
            label = "vehicle name"
        }
    }
}
