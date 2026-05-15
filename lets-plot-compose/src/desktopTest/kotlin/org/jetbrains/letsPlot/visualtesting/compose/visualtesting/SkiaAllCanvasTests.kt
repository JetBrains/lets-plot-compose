/*
 * Copyright (c) 2026. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.visualtesting.compose.visualtesting

import org.jetbrains.letsPlot.compose.canvas.SkiaCanvasPeer
import org.jetbrains.letsPlot.visualtesting.AwtBitmapIO
import org.jetbrains.letsPlot.visualtesting.ImageComparer
import org.jetbrains.letsPlot.visualtesting.canvas.AllCanvasTests
import org.jetbrains.letsPlot.visualtesting.compose.NotoFontManager
import kotlin.test.Test

class SkiaAllCanvasTests {
    @Test
    fun runAllCanvasTests() {
        val awtBitmapIO = AwtBitmapIO(
            expectedImagesDir = "/src/desktopTest/resources/expected-images",
            subdir = "/visual-testing/canvas"
        )
        val canvasPeer = SkiaCanvasPeer(NotoFontManager.INSTANCE)
        val imageComparer = ImageComparer(canvasPeer, awtBitmapIO, silent = true)

        AllCanvasTests.runAllTests(canvasPeer, imageComparer)
    }
}