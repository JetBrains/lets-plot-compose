/*
 * Copyright (c) 2026 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package demo.plot.various

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.jetbrains.letsPlot.compose.PlotPanelRaw
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.sqrt
import kotlin.random.Random

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "GGDeck (Compose Desktop)") {
        MaterialTheme {
            Column(
                modifier = Modifier.fillMaxSize().padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 10.dp),
            ) {
                PlotPanelRaw(
                    rawSpec = ggDeckSpec(),
                    preserveAspectRatio = false,
                    modifier = Modifier.fillMaxSize()
                ) { computationMessages ->
                    computationMessages.forEach { println("[DEMO APP MESSAGE] $it") }
                }
            }
        }
    }
}

private fun ggDeckSpec(): MutableMap<String, Any> {
    val data = randomData(50)

    val linePlotSpec = mutableMapOf<String, Any>(
        "mapping" to mapOf("x" to "x", "y" to "y1"),
        "guides" to mapOf("y" to mapOf("title" to "Line (left)")),
        "kind" to "plot",
        "layers" to listOf(
            mapOf(
                "geom" to "line",
                "color" to "blue",
            )
        ),
        "data" to data,
    )

    val pointsPlotSpec = mutableMapOf<String, Any>(
        "mapping" to mapOf("x" to "x", "y" to "y2"),
        "guides" to mapOf("y" to mapOf("title" to "Points (right)")),
        "theme" to mapOf(
            "axis_text_y" to mapOf("color" to "blue"),
            "axis_ticks_y" to mapOf("color" to "blue"),
            "axis_line_y" to mapOf("color" to "blue"),
        ),
        "kind" to "plot",
        "scales" to listOf(mapOf("aesthetic" to "y", "position" to "right")),
        "layers" to listOf(
            mapOf(
                "geom" to "point",
                "color" to "red",
            )
        ),
        "data" to data,
    )

    return mutableMapOf(
        "theme" to mapOf("name" to "classic"),
        "ggtoolbar" to emptyMap<String, Any>(),
        "kind" to "subplots",
        "layout" to mapOf("name" to "deck"),
        "figures" to listOf(
            linePlotSpec,
            pointsPlotSpec,
        ),
    )
}

private fun randomData(n: Int): Map<String, List<*>> {
    val random = Random(42)
    val x = (0 until n).toList()

    var cumSum = 0.0
    val y1 = List(n) {
        // Box-Muller transform for normal distribution.
        val u1 = random.nextDouble()
        val u2 = random.nextDouble()
        val z0 = sqrt(-2.0 * ln(u1)) * cos(2.0 * PI * u2)

        cumSum += z0
        cumSum
    }

    val y2 = List(n) { random.nextDouble(100.0, 200.0) }

    return mapOf(
        "x" to x,
        "y1" to y1,
        "y2" to y2,
    )
}
