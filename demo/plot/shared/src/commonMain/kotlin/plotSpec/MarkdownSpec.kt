/*
 * Copyright (c) 2024 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package plotSpec

import org.jetbrains.letsPlot.geom.geomBlank
import org.jetbrains.letsPlot.intern.Plot
import org.jetbrains.letsPlot.label.ggtitle
import org.jetbrains.letsPlot.label.labs
import org.jetbrains.letsPlot.label.xlab
import org.jetbrains.letsPlot.label.ylab
import org.jetbrains.letsPlot.letsPlot
import org.jetbrains.letsPlot.scale.scaleColorManual
import org.jetbrains.letsPlot.themes.elementBlank
import org.jetbrains.letsPlot.themes.elementMarkdown
import org.jetbrains.letsPlot.themes.elementText
import org.jetbrains.letsPlot.themes.theme
import org.jetbrains.letsPlot.tooltips.tooltipsNone

class MarkdownSpec : PlotDemoFigure {
    override fun createFigureList(): List<Plot> {
        return listOf(
            mpg(),
            mpgTitleOnly(),
        )
    }

    fun mpgTitleOnly(): Plot {
        return letsPlot() +
                geomBlank(
                    inheritAes = false,
                    tooltips = tooltipsNone
                ) +
                ggtitle(
                    """<span style="color:#66c2a5">**Forward**</span>, <span style="color:#8da0cb">**Rear**</span> and <span style="color:#fc8d62">**4WD**</span> Drivetrain"""
                ) +
                theme(
                    // "text": { "blank": true }
                    text = elementBlank(),

                    // "axis_title": { "blank": true }
                    axisTitle = elementBlank(),

                    // "title": { "markdown": true, "blank": false }
                    title = elementMarkdown(),

                    // "plot_title": { "size": 30.0, "hjust": 0.5, "blank": false }
                    plotTitle = elementText(size = 30.0, hjust = 0.5)
                )
    }

    fun mpg(): Plot {
        return letsPlot() +
                // Layers
                geomBlank(
                    inheritAes = false,
                    tooltips = tooltipsNone
                ) +
                // Scales
                scaleColorManual(
                    values = listOf("#66c2a5", "#fc8d62", "#8da0cb"),
                    guide = "none" // or Guide.NONE
                ) +
                // Labels: Title and Subtitle
                ggtitle(
                    title = """<span style="color:#66c2a5">**Forward**</span>, <span style="color:#8da0cb">**Rear**</span> and <span style="color:#fc8d62">**4WD**</span> Drivetrain""",
                    subtitle = "**City milage** *vs* **displacement**"
                ) +
                // Labels: Caption
                labs(
                    caption = """<span style='color:grey'>Powered by <a href='https://lets-plot.org'>Lets-Plot</a>.  
Visit the <a href='https://github.com/jetbrains/lets-plot/issues'>issue tracker</a> for feedback.</span>"""
                ) +
                // Guides / Axis Labels
                xlab("Displacement (***inches***)") +
                ylab("Miles per gallon (***cty***)") +
                // Theme Configuration
                theme(
                    // "title": { "markdown": true }
                    title = elementMarkdown(),

                    // "plot_title": { "size": 30.0, "hjust": 0.5 }
                    plotTitle = elementText(size = 30.0, hjust = 0.5),

                    // "plot_subtitle": { "hjust": 0.5 }
                    plotSubtitle = elementText(hjust = 0.5)
                )
    }
}
