/*
 * Copyright (c) 2026 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package demo.plot.interact

import androidx.compose.foundation.layout.*
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.jetbrains.letsPlot.compose.PlotFigureModel
import org.jetbrains.letsPlot.compose.PlotPanel
import org.jetbrains.letsPlot.compose.sandbox.SandboxToolbarCmp
import org.jetbrains.letsPlot.core.interact.InteractionSpec
import org.jetbrains.letsPlot.interact.ggtb
import plotSpec.AutoSpec

/**
 * Demo showing:
 * 1. Custom external toolbar using SandboxToolbar
 * 2. Default interactions that can be toggled on/off
 */
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Custom Toolbar & Default Interactions (Compose Desktop)"
    ) {
        MaterialTheme {
            Column(
                modifier = Modifier.fillMaxSize().padding(10.dp)
            ) {
                val figureModel = remember { PlotFigureModel() }
                var enableDefaultInteractions by remember { mutableStateOf(false) }

                // Controls section
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Default Interactions:", modifier = Modifier.padding(end = 8.dp))
                    Checkbox(
                        checked = enableDefaultInteractions,
                        onCheckedChange = { enabled ->
                            enableDefaultInteractions = enabled
                            if (enabled) {
                                figureModel.setDefaultInteractions(
                                    listOf(
                                        InteractionSpec(
                                            InteractionSpec.Name.WHEEL_ZOOM,
                                            keyModifiers = listOf(
                                                InteractionSpec.KeyModifier.CTRL,
                                                InteractionSpec.KeyModifier.SHIFT
                                            )
                                        ),
                                        InteractionSpec(
                                            InteractionSpec.Name.DRAG_PAN,
                                            keyModifiers = listOf(
                                                InteractionSpec.KeyModifier.CTRL,
                                                InteractionSpec.KeyModifier.SHIFT
                                            )
                                        )
                                    )
                                )
                            } else {
                                figureModel.setDefaultInteractions(emptyList())
                            }
                        }
                    )
                    Text(
                        text = "(Ctrl+Shift+Drag to pan, Ctrl+Shift+Wheel to zoom)",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                // Custom external toolbar
                SandboxToolbarCmp(
                    figureModel = figureModel,
                    modifier = Modifier.fillMaxWidth()
                )

                // Plot
                val plot = remember { AutoSpec().scatter() + ggtb() }
                PlotPanel(
                    figure = plot,
                    figureModel = figureModel,
                    preserveAspectRatio = false,
                    modifier = Modifier.fillMaxSize()
                ) { computationMessages ->
                    computationMessages.forEach { println("[DEMO] $it") }
                }
            }
        }
    }
}
