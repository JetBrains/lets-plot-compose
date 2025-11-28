/*
 * Copyright (c) 2025 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle

private const val devRendering = false

@Composable
actual fun PlotPanelRaw(
    rawSpec: MutableMap<String, Any>,
    preserveAspectRatio: Boolean,
    modifier: Modifier,
    errorTextStyle: TextStyle,
    errorModifier: Modifier,
    legacyRendering: Boolean,
    computationMessagesHandler: (List<String>) -> Unit
) {
    Row(modifier = modifier) {
        @Suppress("SimplifyBooleanWithConstants", "KotlinConstantConditions")
        if (!devRendering && !legacyRendering) {
            PlotPanelComposeCanvas(
                rawSpec = rawSpec,
                preserveAspectRatio = preserveAspectRatio,
                modifier = modifier,
                errorTextStyle = errorTextStyle,
                errorModifier = errorModifier,
                computationMessagesHandler = computationMessagesHandler
            )
        } else if (!devRendering && legacyRendering) {
            PlotPanelSwingComponent(
                rawSpec = rawSpec,
                preserveAspectRatio = preserveAspectRatio,
                modifier = modifier,
                errorTextStyle = errorTextStyle,
                errorModifier = errorModifier,
                computationMessagesHandler = computationMessagesHandler
            )
        } else {
            // devRendering == true
            Column(modifier = Modifier.weight(1f)) {
                androidx.compose.material.Text(text = "Skia mapper")
                PlotPanelSwingComponent(
                    rawSpec = rawSpec,
                    preserveAspectRatio = preserveAspectRatio,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    errorTextStyle = errorTextStyle,
                    errorModifier = errorModifier,
                    computationMessagesHandler = computationMessagesHandler
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                androidx.compose.material.Text(text = "Skia Canvas + plot_raster")
                PlotPanelComposeCanvas(
                    rawSpec = rawSpec,
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
