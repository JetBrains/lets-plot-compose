/*
 * Copyright (c) 2024 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package plotSpec

import org.jetbrains.letsPlot.*
import org.jetbrains.letsPlot.coord.coordPolar
import org.jetbrains.letsPlot.geom.*
import org.jetbrains.letsPlot.geom.extras.arrow
import org.jetbrains.letsPlot.intern.Plot
import org.jetbrains.letsPlot.label.labs
import org.jetbrains.letsPlot.scale.scaleShapeIdentity
import org.jetbrains.letsPlot.scale.scaleSizeIdentity
import org.jetbrains.letsPlot.scale.scaleYContinuous
import org.jetbrains.letsPlot.scale.ylim
import org.jetbrains.letsPlot.themes.*
import org.jetbrains.letsPlot.tooltips.layerTooltips
import org.jetbrains.letsPlot.tooltips.tooltipsNone
import kotlin.random.Random

class HyperlinkSpec : PlotDemoFigure {
    override fun createFigureList(): List<Figure> {
        return listOf(
            ggplot() + geomLabel(x=0, y=0, label = "Visit <a href=\"https://lets-plot.org\">lets-plot.org</a> !", size = 10) + themeVoid(),
            lpverse(),
            ggplot() +
                    geomPoint(
                        x = 0,
                        y = 0,
                        size = 20,
                        tooltips = layerTooltips()
                            .title("Lets-Plot\nMultiplatform")
                            .line("Links: <a href=\"https://lets-plot.org\">docs</a>, <a href=\"https://github.com/JetBrains/lets-plot\">sources</a>")
                    )
                    + themeVoid(),
            gggrid(listOf(
                ggplot() + geomLabel(x=0, y=0, label = "Visit <a href=\"https://lets-plot.org\">lets-plot.org</a> !", size = 10) + themeVoid(),
                ggplot() + geomLabel(x=0, y=0, label = "Visit <a href=\"https://lets-plot.org/python/pages/gallery.html\">lets-plot gallery</a> !", size = 10) + themeVoid(),
                ggplot() + geomLabel(x=0, y=0, label = "Visit <a href=\"https://github.com/JetBrains/lets-plot\">lets-plot github</a> !", size = 10) + themeVoid(),
            ))
        )

    }

    fun lpverse(): Plot {
        val random = Random(seed = 42)
        val n = 200
        val noiseData = mapOf(
            "x" to List(n) { random.nextDouble(0.0, 200.0) },
            "y" to List(n) { random.nextDouble(100.0, 400.0) },
            "size" to List(n) { random.nextDouble(0.1, 2.0) }
        )
        val backdrop = geomPoint(data = noiseData, tooltips = tooltipsNone) { x = "x"; y = "y"; size = "size" }

        data class Item(
            val name: String,
            val documentationUrl: String,
            val sourcesUrl: String,
            val x: Double,
            val y: Double,
            val size: Double,
            val shape: Int,
            val angle: Double
        )

        val items = listOf(
            Item(
                name = "Lets-Plot\nMultiplatform",
                documentationUrl = "https://lets-plot.org",
                sourcesUrl = "https://github.com/JetBrains/lets-plot",
                x = 0.0, y = 0.0, size = 14.0, shape = 16, angle = 0.0
            ),
            Item(
                name = "Lets-Plot\nfor Python",
                documentationUrl = "https://lets-plot.org/kotlin/get-started.html",
                sourcesUrl = "https://github.com/JetBrains/lets-plot-kotlin",
                x = 130.0, y = 150.0, size = 9.0, shape = 15, angle = 15.0
            ),
            Item(
                name = "Lets-Plot\nfor Kotlin",
                documentationUrl = "https://lets-plot.org/kotlin/get-started.html",
                sourcesUrl = "https://github.com/JetBrains/lets-plot-kotlin",
                x = 200.0, y = 200.0, size = 9.0, shape = 15, angle = -15.0
            ),
            Item(
                name = "Lets-Plot\nCompose Multiplatform",
                documentationUrl = "https://github.com/JetBrains/lets-plot-compose",
                sourcesUrl = "https://github.com/JetBrains/lets-plot-compose",
                x = 80.0, y = 250.0, size = 7.0, shape = 15, angle = 30.0
            ),
            Item(
                name = "Geocoding",
                documentationUrl = "https://lets-plot.org/python/pages/geocoding.html",
                sourcesUrl = "https://github.com/JetBrains/lets-plot",
                x = 70.0, y = 320.0, size = 7.0, shape = 17, angle = 0.0
            ),
            Item(
                name = "Kandy",
                documentationUrl = "https://kotlin.github.io/kandy/welcome.html",
                sourcesUrl = "https://github.com/Kotlin/kandy",
                x = 195.0, y = 150.0, size = 4.0, shape = 16, angle = 0.0
            ),
        )

        val itemsData = mapOf(
            "name" to items.map(Item::name),
            "documentationUrl" to items.map(Item::documentationUrl),
            "sourcesUrl" to items.map(Item::sourcesUrl),
            "x" to items.map(Item::x),
            "y" to items.map(Item::y),
            "size" to items.map(Item::size),
            "shape" to items.map(Item::shape),
            "angle" to items.map(Item::angle),
        )

        return letsPlot(itemsData) + backdrop +
                geomPoint(
                    showLegend = false,
                    tooltips = layerTooltips()
                        .title("@name")
                        .line("Links: <a href=\"@{documentationUrl}\">docs</a>, <a href=\"@{sourcesUrl}\">sources</a>")
                ) { x = "x"; y = "y"; size = "size"; shape = "shape"; angle = "angle"; color = "name" } +
                // Kandy orbit
                geomPoint(x = 200, y = 200, size = 37, shape = 1, stroke = 0.07) +
                // Kandy stick
                geomSpoke(
                    x = 195,
                    y = 150,
                    angle = -3.14 / 2 / 1.05,
                    radius = 30,
                    size = 1,
                    color = "rgb(255,255,97)"
                ) +
                // Guide
                geomText(
                    x = 50, y = 250, size = 12,
                    label = "Hover, then click\nto <a href=\"https://www.merriam-webster.com/dictionary/freeze\">freeze</a> the tooltip.\nClick links\nto navigate."
                ) +
                geomSegment(
                    x = 70, y = 250, xend = 0, yend = 0,
                    sizeStart = 150, sizeEnd = 20,
                    arrow = arrow(type = "open", angle = 40)
                ) +
                scaleYContinuous(breaks = listOf(150, 200, 250, 320)) +
                scaleShapeIdentity() + scaleSizeIdentity() +
                coordPolar() +
                ylim(listOf(0, 400)) +
                labs(
                    title = "The <a href=\"https://lets-plot.org/python/pages/gallery.html\">Observable</a> LP-verse",
                    subtitle = "Latest <a href=\"https://github.com/JetBrains/lets-plot/releases/latest\">news</a>.",
                    caption = "User <a href=\"https://github.com/JetBrains/lets-plot/issues\">stories</a>."
                ) +
                ggsize(800, 800) +
                theme(
                    plotTitle = elementText(size = 25, face = "bold", hjust = 0.5),
                    plotSubtitle = elementText(hjust = 0.5),
                    plotMargin = listOf(40, 0, 0),
                    axisTitle = "blank",
                    axisText = "blank",
                    axisTicks = "blank",
                    panelGrid = "blank",
                    panelGridMajorY = elementLine(size = 3, linetype = "dotted")
                ) +
                flavorHighContrastDark()
    }

}