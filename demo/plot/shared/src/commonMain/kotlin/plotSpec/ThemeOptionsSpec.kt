/*
 * Copyright (c) 2023 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package plotSpec

import org.jetbrains.letsPlot.Figure
import org.jetbrains.letsPlot.geom.geomPoint
import org.jetbrains.letsPlot.intern.Feature
import org.jetbrains.letsPlot.intern.Plot
import org.jetbrains.letsPlot.label.ggtitle
import org.jetbrains.letsPlot.label.labs
import org.jetbrains.letsPlot.letsPlot
import org.jetbrains.letsPlot.scale.scaleSize
import org.jetbrains.letsPlot.scale.scaleXContinuous
import org.jetbrains.letsPlot.scale.scaleYContinuous
import org.jetbrains.letsPlot.themes.*
import org.jetbrains.letsPlot.tooltips.layerTooltips

class ThemeOptionsSpec : PlotDemoFigure {


    // ToDo: "italic" doesn't seem to work.

    override fun createFigureList(): List<Figure> {
        return listOf(
            // use predefined themes
            withTheme("classic"),
            withTheme("light"),
            withTheme("grey"),
            withTheme("minimal"),
            withTheme("minimal2"),
            withTheme("none"),
            withTheme("bw"),

            setThemeOptions()
        )
    }

    private fun withTheme(themeName: String?): Plot {
        val theme: Feature? = when (themeName) {
            "classic" -> themeClassic()
            "light" -> themeLight()
            "grey" -> themeGrey()
            "minimal" -> themeMinimal()
            "minimal2" -> themeMinimal2()
            "none" -> themeNone()
            "bw" -> themeBW()

            else -> null
        }
        val title = "With theme = ${themeName ?: "default"}"

        return plot(title, theme)
    }

    private fun plot(plotTitle: String, theme: Feature?): Plot {
        val plot = letsPlot(
            data = mapOf(
                "x" to listOf(1, 2, 3, 4),
                "y" to listOf(1, 2, 3, 4)
            )
        ) {
            x = "x"
            y = "y"
            size = "y"
            fill = "y"
        } + geomPoint(
            tooltips = layerTooltips()
                .line("label|value")
                .line("The static text")
                .title("Title")
        ) + ggtitle(plotTitle, subtitle = "The plot subtitle") + labs(caption = "The plot caption") +
                scaleXContinuous("New x-axis label") +
                scaleYContinuous("New y-axis label") +
                scaleSize(name = "New legend title")

        return if (theme == null) {
            plot
        } else {
            plot + theme
        }
    }

    private fun setThemeOptions(): Figure {
        val theme = theme(
            title = elementText(color = "#2a14a8"),
            plotTitle = elementText(face = "bold_italic", family = "Times New Roman"),
            plotCaption = elementText(face = "italic"),
            legendTitle = elementText(face = "bold_italic"),
            tooltipText = elementText(color = "#b3deff", face = "italic"),
            tooltip = elementRect(color = "#2a14a8", fill = "#004d99", size = 2.0),
            axisTitle = elementText(color = "#9b2d30", face = "bold"),
            axisText = elementText(color = "pink", face = "italic"),
            axisTooltipX = elementRect(color = "pink", fill = "#6c4675", size = 2.0),
            axisTooltipTextX = elementText(color = "pink"),
        )

        return plot(
            plotTitle = "User Theme Options",
            theme = theme
        )
    }
}