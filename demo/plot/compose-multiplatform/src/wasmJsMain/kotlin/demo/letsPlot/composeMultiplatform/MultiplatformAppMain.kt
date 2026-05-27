/*
 * Copyright (c) 2026. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package demo.letsPlot.composeMultiplatform

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport


@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(
        viewportContainerId = "ComposeTarget",
        content = { App() }
    )
}