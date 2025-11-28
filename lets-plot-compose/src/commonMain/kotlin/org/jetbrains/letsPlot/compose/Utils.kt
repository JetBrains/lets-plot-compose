package org.jetbrains.letsPlot.compose

import androidx.compose.ui.Modifier

fun containsBackground(modifier: Modifier): Boolean {
    return modifier.foldIn(false) { hasBg, element ->
        hasBg || element.toString().contains("BackgroundElement")
    }
}