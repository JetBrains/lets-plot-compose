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

// Core expect function - works with raw specs
@Suppress("FunctionName")
@Composable
expect fun PlotPanelRaw(
    rawSpec: MutableMap<String, Any>,
    preserveAspectRatio: Boolean,
    modifier: Modifier,
    errorTextStyle: TextStyle = TextStyle(color = Color(0xFF700000)),
    errorModifier: Modifier = Modifier.padding(16.dp),
    computationMessagesHandler: (List<String>) -> Unit
)