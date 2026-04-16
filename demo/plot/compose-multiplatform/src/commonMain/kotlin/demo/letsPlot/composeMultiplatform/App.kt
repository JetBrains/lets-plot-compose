/*
 * Copyright (c) 2026. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package demo.letsPlot.composeMultiplatform

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import org.jetbrains.letsPlot.Figure
import org.jetbrains.letsPlot.compose.PlotPanel
import org.jetbrains.letsPlot.geom.geomDensity
import org.jetbrains.letsPlot.intern.Plot
import org.jetbrains.letsPlot.letsPlot
import org.jetbrains.letsPlot.themes.*
import kotlin.random.Random

@Composable
fun App() {
    val baseFigure = remember { createFigure() }
    var selectedTheme by remember { mutableStateOf("minimal2") }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var isDarkTheme by remember { mutableStateOf(false) }

    val themes = mapOf(
        "minimal2" to themeMinimal2(),
        "bw" to themeBW(),
        "grey" to themeGrey(),
        "classic" to themeClassic(),
        "light" to themeLight(),
        "minimal" to themeMinimal(),
        "void" to themeVoid(),
        "none" to themeNone()
    )

    // Plot theme.
    val themedFigure = themes[selectedTheme]?.let { theme ->
        (baseFigure as Plot) + theme
    } ?: baseFigure

    // Plot flavor.
    val finalFigure = if(isDarkTheme) {
        (themedFigure as Plot) + flavorHighContrastDark()
    } else {
        themedFigure
    }

    val colors = if (isDarkTheme) darkColors() else lightColors()

    MaterialTheme(colors = colors) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 10.dp),
            ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                Text("Plot Theme:")
                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = { dropdownExpanded = true }
                ) {
                    Text(selectedTheme)
                }

                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    themes.keys.forEach { themeName ->
                        DropdownMenuItem(
                            onClick = {
                                selectedTheme = themeName
                                dropdownExpanded = false
                            }
                        ) {
                            Text(themeName)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                IconButton(
                    onClick = { isDarkTheme = !isDarkTheme }
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) LightModeIcon else DarkModeIcon,
                        contentDescription = if (isDarkTheme) "Switch to Light Mode" else "Switch to Dark Mode"
                    )
                }
            }

            PlotPanel(
                figure = finalFigure,
                modifier = Modifier.fillMaxSize()
            ) { computationMessages ->
                computationMessages.forEach { println("[DEMO APP MESSAGE] $it") }
            }
            }
        }
    }
}

fun createFigure(): Figure {
    val rand = Random.Default
    val n = 200
    val xs = List(n) { rand.nextDouble() * 6 - 3 } // Normal-like distribution
    val data = mapOf<String, Any>(
        "x" to xs
    )

    return letsPlot(data) + geomDensity(alpha = 0.2) { x = "x" }
}

private val DarkModeIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "DarkMode",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 3f)
            curveToRelative(-4.97f, 0f, -9f, 4.03f, -9f, 9f)
            reflectiveCurveToRelative(4.03f, 9f, 9f, 9f)
            reflectiveCurveToRelative(9f, -4.03f, 9f, -9f)
            curveToRelative(0f, -0.46f, -0.04f, -0.92f, -0.1f, -1.36f)
            curveToRelative(-0.98f, 1.37f, -2.58f, 2.26f, -4.4f, 2.26f)
            curveToRelative(-2.98f, 0f, -5.4f, -2.42f, -5.4f, -5.4f)
            curveToRelative(0f, -1.82f, 0.89f, -3.42f, 2.26f, -4.4f)
            curveTo(12.92f, 3.04f, 12.46f, 3f, 12f, 3f)
            close()
        }
    }.build()
}

private val LightModeIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "LightMode",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 7f)
            curveToRelative(-2.76f, 0f, -5f, 2.24f, -5f, 5f)
            reflectiveCurveToRelative(2.24f, 5f, 5f, 5f)
            reflectiveCurveToRelative(5f, -2.24f, 5f, -5f)
            reflectiveCurveToRelative(-2.24f, -5f, -5f, -5f)
            close()
            moveTo(2f, 13f)
            lineToRelative(2f, 0f)
            lineToRelative(0f, -2f)
            lineToRelative(-2f, 0f)
            lineToRelative(0f, 2f)
            close()
            moveTo(20f, 13f)
            lineToRelative(2f, 0f)
            lineToRelative(0f, -2f)
            lineToRelative(-2f, 0f)
            lineToRelative(0f, 2f)
            close()
            moveTo(11f, 2f)
            lineToRelative(0f, 2f)
            lineToRelative(2f, 0f)
            lineToRelative(0f, -2f)
            lineToRelative(-2f, 0f)
            close()
            moveTo(11f, 20f)
            lineToRelative(0f, 2f)
            lineToRelative(2f, 0f)
            lineToRelative(0f, -2f)
            lineToRelative(-2f, 0f)
            close()
            moveTo(5.99f, 4.58f)
            lineToRelative(-1.41f, 1.41f)
            lineToRelative(1.06f, 1.06f)
            lineToRelative(1.41f, -1.41f)
            lineToRelative(-1.06f, -1.06f)
            close()
            moveTo(18.36f, 16.95f)
            lineToRelative(-1.41f, 1.41f)
            lineToRelative(1.06f, 1.06f)
            lineToRelative(1.41f, -1.41f)
            lineToRelative(-1.06f, -1.06f)
            close()
            moveTo(16.95f, 5.64f)
            lineToRelative(1.41f, -1.06f)
            lineToRelative(1.06f, 1.41f)
            lineToRelative(-1.41f, 1.06f)
            lineToRelative(-1.06f, -1.41f)
            close()
            moveTo(4.58f, 18.01f)
            lineToRelative(1.41f, -1.06f)
            lineToRelative(1.06f, 1.41f)
            lineToRelative(-1.41f, 1.06f)
            lineToRelative(-1.06f, -1.41f)
            close()
        }
    }.build()
}