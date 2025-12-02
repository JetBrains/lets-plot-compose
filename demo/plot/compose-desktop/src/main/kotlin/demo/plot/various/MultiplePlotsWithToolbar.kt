/*
 * Copyright (c) 2025 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package demo.plot.various

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.jetbrains.letsPlot.compose.PlotPanel
import org.jetbrains.letsPlot.interact.ggtb
import plotSpec.AutoSpec
import plotSpec.MarkdownSpec

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Multiple Plot Weight Layout (Compose Desktop)") {
        MaterialTheme {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PlotPanel(
                    figure = AutoSpec().scatter() + ggtb(),
                    modifier = Modifier.fillMaxHeight().weight(1f)
                ) { computationMessages ->
                    computationMessages.forEach { println("[DEMO APP MESSAGE] $it") }
                }

                PlotPanel(
                    figure = MarkdownSpec().mpg() + ggtb(),
                    preserveAspectRatio = false,
                    modifier = Modifier.fillMaxHeight().weight(1f)
                ) { computationMessages ->
                    computationMessages.forEach { println("[DEMO APP MESSAGE] $it") }
                }
            }
        }
    }
}
