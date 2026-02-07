/*
 * Copyright (c) 2025 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle

private const val devRendering = false

/**
 * Android implementation of [PlotPanelRaw].
 */
@Composable
actual fun PlotPanelRaw(
    rawSpec: MutableMap<String, Any>,
    figureModel: PlotFigureModel?,
    preserveAspectRatio: Boolean,
    modifier: Modifier,
    errorTextStyle: TextStyle,
    errorModifier: Modifier,
    legacyRendering: Boolean,
    computationMessagesHandler: (List<String>) -> Unit
) {
    val actualFigureModel = figureModel ?: remember { PlotFigureModel() }

    // Dispose internally-created figureModel when this component is removed
    if (figureModel == null) {
        DisposableEffect(actualFigureModel) {
            onDispose {
                actualFigureModel.dispose()
            }
        }
    }

    Row(modifier = modifier) {
        @Suppress("SimplifyBooleanWithConstants", "KotlinConstantConditions")
        if (!devRendering && !legacyRendering) {
            PlotPanelComposeCanvas(
                rawSpec = rawSpec,
                figureModel = actualFigureModel,
                preserveAspectRatio = preserveAspectRatio,
                modifier = modifier,
                errorTextStyle = errorTextStyle,
                errorModifier = errorModifier,
                computationMessagesHandler = computationMessagesHandler
            )
        } else if (!devRendering && legacyRendering) {
            PlotPanelAndroidView(
                rawSpec = rawSpec,
                figureModel = actualFigureModel,
                preserveAspectRatio = preserveAspectRatio,
                modifier = modifier,
                errorTextStyle = errorTextStyle,
                errorModifier = errorModifier,
                computationMessagesHandler = computationMessagesHandler
            )

        } else {
            Column(modifier = Modifier.weight(1f)) {
                BasicText(text = "Android View")
                PlotPanelAndroidView(
                    rawSpec = rawSpec,
                    figureModel = actualFigureModel,
                    preserveAspectRatio = preserveAspectRatio,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    errorTextStyle = errorTextStyle,
                    errorModifier = errorModifier,
                    computationMessagesHandler = computationMessagesHandler
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                BasicText(text = "Compose Canvas")
                PlotPanelComposeCanvas(
                    rawSpec = rawSpec,
                    figureModel = actualFigureModel,
                    preserveAspectRatio = preserveAspectRatio,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    errorTextStyle = errorTextStyle,
                    errorModifier = errorModifier,
                    computationMessagesHandler = computationMessagesHandler
                )
            }
        }
    }
}
