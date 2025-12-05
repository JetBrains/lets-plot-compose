/*
 * Copyright (c) 2024 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.compose

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.letsPlot.commons.registration.Registration
import org.jetbrains.letsPlot.core.plot.builder.interact.tools.DefaultFigureToolsController
import org.jetbrains.letsPlot.core.plot.builder.interact.tools.ToggleTool
import org.jetbrains.letsPlot.core.plot.builder.interact.tools.ToggleToolModel
import org.jetbrains.letsPlot.core.plot.builder.interact.tools.ToolSpecs.BBOX_ZOOM_TOOL_SPEC
import org.jetbrains.letsPlot.core.plot.builder.interact.tools.ToolSpecs.CBOX_ZOOM_TOOL_SPEC
import org.jetbrains.letsPlot.core.plot.builder.interact.tools.ToolSpecs.PAN_TOOL_SPEC
import org.jetbrains.letsPlot.core.plot.builder.interact.tools.res.ToolbarIcons


@Suppress("FunctionName")
@Composable
fun PlotToolbar(figureModel: PlotFigureModel) {
    var registration by remember { mutableStateOf(Registration.EMPTY) }
    var panToolState by remember { mutableStateOf(false) }
    var bboxZoomToolState by remember { mutableStateOf(false) }
    var cboxZoomToolState by remember { mutableStateOf(false) }

    val panTool = remember { ToggleTool(PAN_TOOL_SPEC) }
    val bboxZoomTool = remember { ToggleTool(BBOX_ZOOM_TOOL_SPEC) }
    val cboxZoomTool = remember { ToggleTool(CBOX_ZOOM_TOOL_SPEC) }

    val controller = remember {
        DefaultFigureToolsController(figureModel, errorMessageHandler = { println(it) }).also {
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

    // Toolbar container - matches PlotPanelToolbar.kt (Swing)
    //
    // Expected height: 33px
    // (org.jetbrains.letsPlot.core.plot.builder.presentation.Defaults.TOOLBAR_HEIGHT)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(33.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(C_BACKGR_TRANSPARENT)
                .border(
                    BorderStroke(1.dp, Color(200, 200, 200)),
                    RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 5.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SvgIconButton(
                svgString = ToolbarIcons.PAN_TOOL,
                isSelected = panToolState,
                onClick = { panToolModel.action() },
                contentDescription = "Pan"
            )

            SvgIconButton(
                svgString = ToolbarIcons.ZOOM_CORNER,
                isSelected = bboxZoomToolState,
                onClick = { bboxZoomToolModel.action() },
                contentDescription = "Rubber Band Zoom"
            )

            SvgIconButton(
                svgString = ToolbarIcons.ZOOM_CENTER,
                isSelected = cboxZoomToolState,
                onClick = { cboxZoomToolModel.action() },
                contentDescription = "Centerpoint Zoom"
            )

            SvgIconButton(
                svgString = ToolbarIcons.RESET,
                isSelected = false,
                onClick = { controller.resetFigure(deactiveTools = true) },
                contentDescription = "Reset"
            )
        }
    }
}

@Composable
private fun SvgIconButton(
    svgString: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val iconColor = when {
        isSelected -> C_STROKE_SEL  // White when selected
        else -> C_STROKE             // Gray for normal and hover
    }

    val backgroundColor = when {
        isSelected -> C_BACKGR_SEL           // Blue when selected
        isHovered -> C_BACKGR_HOVER          // Light gray when hovering
        else -> Color.Transparent            // Transparent for a normal state
    }

    val icon = SvgIconUtils.rememberSvgIcon(
        svgString = svgString,
        iconColor = iconColor
    )

    Box(
        modifier = modifier
            .size(22.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .hoverable(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(16.dp)
        )
    }
}

private val C_BACKGR = Color(247, 248, 250)
private val C_STROKE = Color(110, 110, 110)
private val C_BACKGR_HOVER = Color(218, 219, 221)
private val C_BACKGR_SEL = Color(69, 114, 232)
private val C_STROKE_SEL = Color.White

private const val ALPHA = 0.8f

// C_BACKGR with an alpha channel which on a white background looks the same as the solid C_BACKGR
// and slightly darkens any darker background.
private val C_BACKGR_TRANSPARENT = Color(
    red = (C_BACKGR.red - 1.0f * (1 - ALPHA)) / ALPHA,
    green = (C_BACKGR.green - 1.0f * (1 - ALPHA)) / ALPHA,
    blue = (C_BACKGR.blue - 1.0f * (1 - ALPHA)) / ALPHA,
    alpha = ALPHA
)

