/*
 * Copyright (c) 2026 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.compose.sandbox

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.letsPlot.commons.registration.Registration
import org.jetbrains.letsPlot.core.plot.builder.interact.tools.*
import org.jetbrains.letsPlot.core.plot.builder.interact.tools.ToolSpecs.BBOX_ZOOM_TOOL_SPEC
import org.jetbrains.letsPlot.core.plot.builder.interact.tools.ToolSpecs.CBOX_ZOOM_TOOL_SPEC
import org.jetbrains.letsPlot.core.plot.builder.interact.tools.ToolSpecs.PAN_TOOL_SPEC

/**
 * A simple example implementation of an interactive plot toolbar for Compose applications.
 *
 * This composable demonstrates how to create a custom toolbar with pan, zoom, and reset functionality
 * for Lets-Plot figures in Compose-based applications. It provides a basic UI with text-based buttons
 * that can be easily customized for specific application needs.
 *
 * The toolbar connects to a plot via a shared [FigureModel]. The same FigureModel instance can be
 * reused across multiple plots - the toolbar will automatically work with whichever plot is currently
 * displayed.
 *
 * ## Usage Example
 * ```kotlin
 * val figureModel = remember { PlotFigureModel() }
 *
 * Column {
 *     SandboxToolbarCmp(figureModel = figureModel)
 *     PlotPanel(
 *         figure = myPlot,
 *         figureModel = figureModel,
 *         modifier = Modifier.fillMaxSize()
 *     )
 * }
 * ```
 *
 * ## Customization
 * Developers integrating Lets-Plot into their Compose applications can copy this code and modify:
 * - Button appearance and styling
 * - Layout and positioning
 * - Error message handling
 */
@Composable
fun SandboxToolbarCmp(
    figureModel: FigureModel,
    modifier: Modifier = Modifier
) {
    var registration by remember { mutableStateOf(Registration.EMPTY) }
    var panToolState by remember { mutableStateOf(false) }
    var bboxZoomToolState by remember { mutableStateOf(false) }
    var cboxZoomToolState by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val panTool = remember { ToggleTool(PAN_TOOL_SPEC) }
    val bboxZoomTool = remember { ToggleTool(BBOX_ZOOM_TOOL_SPEC) }
    val cboxZoomTool = remember { ToggleTool(CBOX_ZOOM_TOOL_SPEC) }

    val controller = remember {
        DefaultFigureToolsController(figureModel, errorMessageHandler = { errorMessage = it }).also {
            registration = figureModel.addToolEventCallback { event ->
                it.handleToolFeedback(event)
            }
        }
    }

    val panToolModel = remember {
        object : ToggleToolModel() {
            override fun setState(selected: Boolean) {
                panToolState = selected
            }
        }.also { controller.registerTool(panTool, it) }
    }

    val bboxZoomToolModel = remember {
        object : ToggleToolModel() {
            override fun setState(selected: Boolean) {
                bboxZoomToolState = selected
            }
        }.also { controller.registerTool(bboxZoomTool, it) }
    }

    val cboxZoomToolModel = remember {
        object : ToggleToolModel() {
            override fun setState(selected: Boolean) {
                cboxZoomToolState = selected
            }
        }.also { controller.registerTool(cboxZoomTool, it) }
    }

    DisposableEffect(figureModel) {
        onDispose {
            registration.remove()
        }
    }

    // Toolbar container
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(Color(200, 230, 255))
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ToolbarButton(
            text = if (panToolState) "< Pan >" else "Pan",
            onClick = { panToolModel.action() }
        )

        ToolbarButton(
            text = if (bboxZoomToolState) "< Rubber Band Zoom >" else "Rubber Band Zoom",
            onClick = { bboxZoomToolModel.action() }
        )

        ToolbarButton(
            text = if (cboxZoomToolState) "< Centerpoint Zoom >" else "Centerpoint Zoom",
            onClick = { cboxZoomToolModel.action() }
        )

        ToolbarButton(
            text = "Reset",
            onClick = { controller.resetFigure(deactiveTools = true) }
        )

        errorMessage?.let { error ->
            Spacer(modifier = Modifier.width(16.dp))
            BasicText(
                text = error,
                style = TextStyle(color = Color.Red, fontSize = 14.sp),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
private fun ToolbarButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .height(32.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(220, 220, 220))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        BasicText(
            text = text,
            style = TextStyle(color = Color.Black, fontSize = 14.sp)
        )
    }
}
