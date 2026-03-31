/*
 * Copyright (c) 2026. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package demo.plot.multiplatform

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.letsPlot.Figure
import org.jetbrains.letsPlot.compose.PlotFigureModel
import org.jetbrains.letsPlot.compose.PlotPanel
import org.jetbrains.letsPlot.compose.sandbox.SandboxToolbarCmp
import plotSpec.PlotGridSpec

@Composable
fun App(showToolbar: Boolean) {
    val figure = remember { demoFigure() }
    val figureModel = remember { PlotFigureModel() }

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Lets-Plot Compose Multiplatform Demo",
                style = MaterialTheme.typography.h6
            )

            if (showToolbar) {
                SandboxToolbarCmp(
                    figureModel = figureModel
                )
            }

            PlotPanel(
                figure = figure,
                figureModel = figureModel,
                modifier = Modifier.fillMaxSize()
            ) { computationMessages ->
                computationMessages.forEach { println("[DEMO APP MESSAGE] $it") }
            }
        }
    }
}

private fun demoFigure(): Figure {
    return PlotGridSpec().createFigure()
}
