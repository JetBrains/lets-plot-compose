/*
 * Copyright (c) 2026. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package demo.plot.various

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.jetbrains.letsPlot.compose.PlotPanel
import org.jetbrains.letsPlot.geom.geomLine
import org.jetbrains.letsPlot.geom.geomPoint
import org.jetbrains.letsPlot.label.ggtitle
import org.jetbrains.letsPlot.label.xlab
import org.jetbrains.letsPlot.label.ylab
import org.jetbrains.letsPlot.letsPlot
import org.jetbrains.letsPlot.scale.scaleXDateTime
import org.jetbrains.letsPlot.tooltips.layerTooltips
import kotlin.time.Instant

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "kotlinx.datetime Instant (Compose Desktop)") {
        MaterialTheme {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            ) {
                val plot = remember { instantPlot() }
                PlotPanel(
                    figure = plot,
                    preserveAspectRatio = false,
                    modifier = Modifier.fillMaxSize()
                ) { computationMessages ->
                    computationMessages.forEach { println("[DEMO APP MESSAGE] $it") }
                }
            }
        }
    }
}

private fun instantPlot() = letsPlot(
    mapOf(
        "instant" to listOf(
            Instant.parse("2026-05-01T00:00:00Z"),
            Instant.parse("2026-05-01T06:00:00Z"),
            Instant.parse("2026-05-01T12:00:00Z"),
            Instant.parse("2026-05-01T18:00:00Z"),
            Instant.parse("2026-05-02T00:00:00Z"),
            Instant.parse("2026-05-02T06:00:00Z")
        ),
        "throughput" to listOf(18.0, 24.0, 21.5, 28.0, 25.0, 31.0)
    )
) {
    x = "instant"
    y = "throughput"
} +
        geomLine(
            color = "#4C78A8",
            size = 1.6,
            tooltips = layerTooltips()
                .line("@instant|@throughput req/s")
                .format("instant", "%Y-%m-%d %H:%M UTC")
        ) +
        geomPoint(
            color = "#E45756",
            size = 4.0,
            tooltips = layerTooltips()
                .line("@instant|@throughput req/s")
                .format("instant", "%Y-%m-%d %H:%M UTC")
        ) +
        scaleXDateTime(format = "%b %d\n%H:%M") +
        ggtitle("kotlinx.datetime.Instant values in plot") +
        xlab("Instant (UTC)") +
        ylab("Throughput")
