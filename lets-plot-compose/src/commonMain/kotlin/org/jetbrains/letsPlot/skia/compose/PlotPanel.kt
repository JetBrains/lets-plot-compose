/*
 * Copyright (c) 2023 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.skia.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import org.jetbrains.letsPlot.Figure
import org.jetbrains.letsPlot.intern.toSpec

@Suppress("FunctionName")
@Composable
fun PlotPanel(
    figure: Figure,
    preserveAspectRatio: Boolean = false,
    modifier: Modifier,
    computationMessagesHandler: (List<String>) -> Unit
) {
    // Cache the raw spec conversion to avoid recomputing on every recomposition
    val rawSpec = remember(figure) { figure.toSpec() }

    PlotPanelRaw(
        rawSpec = rawSpec,
        preserveAspectRatio = preserveAspectRatio,
        modifier = modifier,
        computationMessagesHandler = computationMessagesHandler
    )
}