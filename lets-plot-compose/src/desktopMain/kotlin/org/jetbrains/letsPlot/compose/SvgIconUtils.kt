/*
 * Copyright (c) 2024 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import org.jetbrains.compose.resources.decodeToSvgPainter

object SvgIconUtils {

    /**
     * Creates a Painter from an SVG string with the specified icon color.
     *
     * @param svgString The SVG content as a string
     * @param iconColor The color to apply to the SVG icon
     * @return A Painter that can be used with Image composable
     */
    @Composable
    fun rememberSvgIcon(
        svgString: String,
        iconColor: Color
    ): Painter {
        val density = LocalDensity.current

        return remember(svgString, iconColor, density) {
            createSvgPainter(svgString, iconColor, density)
        }
    }

    private fun createSvgPainter(
        svgString: String,
        iconColor: Color,
        density: androidx.compose.ui.unit.Density
    ): Painter {
        // Apply color transformation to SVG string
        val coloredSvg = svgString.replace(
            """stroke="none"""",
            """stroke="none" fill="${colorToHex(iconColor)}""""
        )

        return coloredSvg.toByteArray().decodeToSvgPainter(density)
    }

    private fun colorToHex(color: Color): String {
        val red = (color.red * 255).toInt()
        val green = (color.green * 255).toInt()
        val blue = (color.blue * 255).toInt()
        return "#%02x%02x%02x".format(red, green, blue)
    }
}
