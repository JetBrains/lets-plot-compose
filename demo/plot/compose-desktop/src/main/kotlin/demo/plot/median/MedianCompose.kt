/*
 * Copyright (c) 2023 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */


package demo.plot.median

import androidx.compose.foundation.layout.*
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import demo.plot.median.ui.DemoList
import org.jetbrains.letsPlot.Figure
import org.jetbrains.letsPlot.compose.PlotPanelRaw
import org.jetbrains.letsPlot.intern.toSpec
import plotSpec.*

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Demo (Compose Desktop, median)") {

        val figures = listOf(
            "Density Plot" to DensitySpec().createFigure(),
            "Plot Grid" to PlotGridSpec().createFigure(),
            "25k Points" to PerfSpec().createFigure(),
            "BackendError" to IllegalArgumentSpec().createFigure(),
            "FrontendError" to FrontendExceptionSpec().createRawSpec(),
        )

        val preserveAspectRatio = remember { mutableStateOf(false) }
        val figureIndex = remember { mutableStateOf(0) }

        MaterialTheme {
            Row {
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .align(CenterVertically)
                        .width(IntrinsicSize.Max)
                ) {
                    Text("Demos:", fontWeight = FontWeight.Bold)
                    DemoList(
                        options = figures.unzip().first,
                        selectedIndex = figureIndex,
                    )
                    Row {
                        Text(
                            text = "Keep ratio:",
                            modifier = Modifier
                                .align(CenterVertically)
                        )
                        Checkbox(preserveAspectRatio.value, onCheckedChange = { preserveAspectRatio.value = it })
                    }
                }
                Column(
                    modifier = Modifier.fillMaxSize().padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 10.dp),
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                            .padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 10.dp),
                    ) {
                        // Cast to rawSpec to use only the PlotPanelRaw
                        // Switch between PlotPanel and PlotPanelRaw causes re-creation of the whole panel
                        // and hides bugs related to the panel re-use, like OK -> ERROR -> OK state transition.
                        val rawSpec: Map<*, *> = when (val fig = figures[figureIndex.value].second) {
                            is Figure -> fig.toSpec()
                            is Map<*, *> -> fig
                            else -> throw IllegalStateException("Unexpected figure type: ${fig.let { it::class }}")
                        }

                        @Suppress("UNCHECKED_CAST")
                        PlotPanelRaw(
                            rawSpec = rawSpec as MutableMap<String, Any>,
                            preserveAspectRatio = preserveAspectRatio.value,
                            modifier = Modifier.fillMaxSize()
                        ) { computationMessages ->
                            computationMessages.forEach { println("[DEMO APP MESSAGE] $it") }
                        }
                    }
                }
            }
        }
    }
}

