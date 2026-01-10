/*
 * Copyright (c) 2025 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle

@Composable
actual fun PlotPanelRaw(
    rawSpec: MutableMap<String, Any>,
    preserveAspectRatio: Boolean,
    modifier: Modifier,
    errorTextStyle: TextStyle,
    errorModifier: Modifier,
    computationMessagesHandler: (List<String>) -> Unit
) {
    Row(modifier = modifier) {
        PlotPanelComposeCanvas(
            rawSpec = rawSpec,
            preserveAspectRatio = preserveAspectRatio,
            modifier = modifier,
            errorTextStyle = errorTextStyle,
            errorModifier = errorModifier,
            computationMessagesHandler = computationMessagesHandler
        )
    }
}
