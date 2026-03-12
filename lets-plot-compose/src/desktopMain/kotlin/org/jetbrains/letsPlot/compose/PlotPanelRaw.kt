/*
 * Copyright (c) 2025 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle

/**
 * Desktop implementation of [PlotPanelRaw].
 */
@Composable
actual fun PlotPanelRaw(
    rawSpec: MutableMap<String, Any>,
    figureModel: PlotFigureModel?,
    preserveAspectRatio: Boolean,
    modifier: Modifier,
    errorTextStyle: TextStyle,
    errorModifier: Modifier,
    computationMessagesHandler: (List<String>) -> Unit
) {
    val actualFigureModel = figureModel ?: remember { PlotFigureModel() }

    // Dispose internally created figureModel when this component is removed
    if (figureModel == null) {
        DisposableEffect(actualFigureModel) {
            onDispose {
                actualFigureModel.dispose()
            }
        }
    }

    Row(modifier = modifier) {
        PlotPanelComposeCanvas(
            rawSpec = rawSpec,
            figureModel = actualFigureModel,
            preserveAspectRatio = preserveAspectRatio,
            modifier = modifier,
            errorTextStyle = errorTextStyle,
            errorModifier = errorModifier,
            computationMessagesHandler = computationMessagesHandler
        )
    }
}
