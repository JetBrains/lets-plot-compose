/*
 * Copyright (c) 2026. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.visualtesting.compose.visualtesting

import org.jetbrains.letsPlot.compose.canvas.SkiaCanvasPeer
import org.jetbrains.letsPlot.visualtesting.AwtBitmapIO
import org.jetbrains.letsPlot.visualtesting.ImageComparer
import org.jetbrains.letsPlot.visualtesting.compose.NotoFontManager
import org.jetbrains.letsPlot.visualtesting.plot.AllPlotTests
import org.jetbrains.letsPlot.visualtesting.plot.PlotInteractivityTest
import kotlin.test.Test

class SkiaAllPlotTests {
    val awtBitmapIO = AwtBitmapIO(
        expectedImagesDir = "/src/desktopTest/resources/expected-images",
        subdir = "visual-testing/plot"
    )

    val canvasPeer = SkiaCanvasPeer(NotoFontManager.INSTANCE)
    val imageComparer = ImageComparer(canvasPeer, awtBitmapIO, silent = true)

    @Test
    fun runAllPlotTests() {
        AllPlotTests.runAllTests(canvasPeer, imageComparer)
    }

    @Test
    fun runSinglePlotTest() {
        val testSuit = PlotInteractivityTest(canvasPeer, imageComparer)
        testSuit.assertTest(testSuit::plot_interactivity_nestedComposite_tooltip)
    }
}
