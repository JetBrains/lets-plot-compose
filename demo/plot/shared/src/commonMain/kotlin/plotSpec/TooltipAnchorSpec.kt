/*
 * Copyright (c) 2023 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package plotSpec

import demoData.Iris
import org.jetbrains.letsPlot.Figure
import org.jetbrains.letsPlot.Stat
import org.jetbrains.letsPlot.commons.intern.random.RandomGaussian
import org.jetbrains.letsPlot.geom.geomArea
import org.jetbrains.letsPlot.geom.geomBoxplot
import org.jetbrains.letsPlot.label.ggtitle
import org.jetbrains.letsPlot.letsPlot
import org.jetbrains.letsPlot.tooltips.layerTooltips
import kotlin.random.Random

class TooltipAnchorSpec : PlotDemoFigure {

    override fun createFigureList(): List<Figure> {
        return listOf(
            topRight(),
            topLeft(),
            topCenter(),
            bottomRight(),
            bottomLeft(),
            bottomCenter(),
            middleRight(),
            middleLeft(),
            middleCenter(),
            overCursor()
        )
    }

    companion object {
        // ToDo: nice to have it available in LP.
        private fun gauss(count: Int, seed: Long, mean: Double, stdDeviance: Double): List<Double> {
            val r = RandomGaussian(Random(seed))
            return List(count) { r.nextGaussian() * stdDeviance + mean }
        }

        private fun data(): Map<String, List<*>> {
            val count1 = 20
            val count2 = 50
            val ratingA = gauss(count1, 12, 0.0, 1.0)
            val ratingB = gauss(count2, 24, 0.0, 1.0)
            val rating = ratingA + ratingB
//            val cond = DemoUtil.zip(DemoUtil.fill("a", count1), DemoUtil.fill("b", count2))
            val cond = List(count1) { "a" } + List(count2) { "b" }
            val map = HashMap<String, List<*>>()
            map["cond"] = cond
            map["rating"] = rating
            return map
        }

        private fun withTooltipAnchor(anchor: String): Figure {
            val allPositionals = "^Y"
            val aesYMin = "^ymin"
            val aesYMax = "^ymax"
            val aesMiddle = "^middle"
            val aesLower = "^lower"
            val aesUpper = "^upper"

            return letsPlot(data()) {
                x = "cond"
                y = "rating"
                fill = "cond"
            } + geomBoxplot(
                tooltips = layerTooltips()
                    .format(allPositionals, ".0f")
                    .format(aesMiddle, ".2f")
                    .line("min/max|$aesYMin/$aesYMax")
                    .line("lower/upper|$aesLower/$aesUpper")
                    .line("@|$aesMiddle")
                    .anchor(anchor)
            ) + ggtitle("Anchor: \"$anchor\"")
        }

        private fun middleRight(): Figure = withTooltipAnchor("middle_right")

        private fun middleCenter(): Figure = withTooltipAnchor("middle_center")

        private fun middleLeft(): Figure = withTooltipAnchor("middle_left")

        private fun topRight(): Figure = withTooltipAnchor("top_right")

        private fun topLeft(): Figure = withTooltipAnchor("top_left")

        private fun topCenter(): Figure = withTooltipAnchor("top_center")

        private fun bottomRight(): Figure = withTooltipAnchor("bottom_right")

        private fun bottomLeft(): Figure = withTooltipAnchor("bottom_left")

        private fun bottomCenter(): Figure = withTooltipAnchor("bottom_center")

        private fun overCursor(): Figure {
            return letsPlot(Iris.map()) {
                x = "sepal length (cm)"
                color = "sepal width (cm)"
                fill = "target"
                group = "target"
            } + geomArea(
                stat = Stat.density(),
                alpha = 0.7,
                tooltips = layerTooltips().anchor("top_right")
            )
        }
    }
}