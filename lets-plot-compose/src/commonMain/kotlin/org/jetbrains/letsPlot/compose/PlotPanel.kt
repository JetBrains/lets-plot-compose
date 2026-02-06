/*
 * Copyright (c) 2023 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import org.jetbrains.letsPlot.Figure
import org.jetbrains.letsPlot.core.plot.builder.interact.tools.FigureModel
import org.jetbrains.letsPlot.intern.toSpec

/**
 * Displays a plot figure.
 *
 * @param figure The plot figure to display
 * @param figureModel Optional [FigureModel] for controlling plot interactions programmatically.
 *                    Use [rememberPlotFigureModel] to create a model that can be accessed externally.
 * @param preserveAspectRatio Whether to preserve the plot's aspect ratio
 * @param modifier Modifier for the plot container
 * @param errorTextStyle Text style for error messages
 * @param errorModifier Modifier for error message container
 * @param legacyRendering Whether to use legacy rendering (SVG-based)
 * @param computationMessagesHandler Callback for computation messages
 *
 * Example with external FigureModel:
 * ```kotlin
 * val figureModel = rememberPlotFigureModel()
 *
 * PlotPanel(
 *     figure = myPlot,
 *     figureModel = figureModel,
 *     modifier = Modifier.fillMaxSize(),
 *     computationMessagesHandler = { messages -> println(messages) }
 * )
 *
 * // Control the plot programmatically.
 * // For exampl, set figure default interactions.
 * val defaultInteractions = listOf(
 *     InteractionSpec(
 *         InteractionSpec.Name.WHEEL_ZOOM,
 *         keyModifiers = listOf(
 *             InteractionSpec.KeyModifier.CTRL,
 *             InteractionSpec.KeyModifier.SHIFT
 *         )
 *     ),
 *     InteractionSpec(
 *         InteractionSpec.Name.DRAG_PAN,
 *         keyModifiers = listOf(
 *             InteractionSpec.KeyModifier.CTRL,
 *             InteractionSpec.KeyModifier.SHIFT
 *         )
 *     )
 * )
 *
 * figureModel.setDefaultInteractions(defaultInteractions)
 * ```
 */
@Suppress("FunctionName")
@Composable
fun PlotPanel(
    figure: Figure,
    figureModel: FigureModel? = null,
    preserveAspectRatio: Boolean = false,
    modifier: Modifier,
    errorTextStyle: TextStyle = TextStyle(color = Color(0xFF700000)),
    errorModifier: Modifier = Modifier.padding(16.dp),
    legacyRendering: Boolean = false,
    computationMessagesHandler: (List<String>) -> Unit
) {
    // Cache the raw spec conversion to avoid recomputing on every recomposition
    val rawSpec = remember(figure) { figure.toSpec() }

    PlotPanelRaw(
        rawSpec = rawSpec,
        figureModel = figureModel,
        preserveAspectRatio = preserveAspectRatio,
        modifier = modifier,
        errorTextStyle = errorTextStyle,
        errorModifier = errorModifier,
        legacyRendering = legacyRendering,
        computationMessagesHandler = computationMessagesHandler
    )
}