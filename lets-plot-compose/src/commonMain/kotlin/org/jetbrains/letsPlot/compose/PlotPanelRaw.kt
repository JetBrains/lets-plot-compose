/*
 * Copyright (c) 2025 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import org.jetbrains.letsPlot.core.plot.builder.interact.tools.FigureModel

/**
 * Displays a plot figure.
 *
 * @param rawSpec Raw plot specification map
 * @param figureModel Optional [FigureModel] for controlling plot interactions programmatically.
 * @param preserveAspectRatio Whether to preserve the plot's aspect ratio
 * @param modifier Modifier for the plot container
 * @param errorTextStyle Text style for error messages
 * @param errorModifier Modifier for error message container
 * @param legacyRendering Whether to use legacy rendering (SVG-based)
 * @param computationMessagesHandler Callback for computation messages
 *
 * Example with external FigureModel:
 * ```kotlin
 * val figureModel = remember { PlotFigureModel() }
 *
 * PlotPanelRaw(
 *     rawSpec = myRawSpec,
 *     figureModel = figureModel,
 *     modifier = Modifier.fillMaxSize(),
 *     computationMessagesHandler = { messages -> println(messages) }
 * )
 *
 * // Control the plot programmatically.
 * // For example, set figure default interactions.
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
expect fun PlotPanelRaw(
    rawSpec: MutableMap<String, Any>,
    figureModel: PlotFigureModel? = null,
    preserveAspectRatio: Boolean,
    modifier: Modifier,
    errorTextStyle: TextStyle = TextStyle(color = Color(0xFF700000)),
    errorModifier: Modifier = Modifier.padding(16.dp),
    legacyRendering: Boolean = false,
    computationMessagesHandler: (List<String>) -> Unit
)