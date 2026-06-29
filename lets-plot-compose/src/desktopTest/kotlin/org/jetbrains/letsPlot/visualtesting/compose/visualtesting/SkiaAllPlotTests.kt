/*
 * Copyright (c) 2026. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.visualtesting.compose.visualtesting

import org.jetbrains.letsPlot.compose.canvas.SkiaCanvasPeer
import org.jetbrains.letsPlot.visualtesting.AwtBitmapIO
import org.jetbrains.letsPlot.visualtesting.ImageComparer
import org.jetbrains.letsPlot.visualtesting.ImageComparer.ComparisonProfile
import org.jetbrains.letsPlot.visualtesting.compose.NotoFontManager
import org.jetbrains.letsPlot.visualtesting.plot.AllPlotTests
import org.jetbrains.letsPlot.visualtesting.plot.PlotInteractivityTest
import kotlin.test.Test

class SkiaAllPlotTests {
    private val awtBitmapIO = AwtBitmapIO(
        expectedImagesDir = "/src/desktopTest/resources/expected-images",
        subdir = "visual-testing/plot"
    )

    private val skiaCanvasPeer = SkiaCanvasPeer(NotoFontManager.INSTANCE)

    private val imageComparer = ImageComparer(
        canvasPeer = skiaCanvasPeer,
        bitmapIO = awtBitmapIO,
        profileAdjuster = { SKIA_PLOT_COMPARISON_PROFILE }, // use relaxed profile - skia produces unstable images
        silent = true
    )

    @Test
    fun runAllPlotTests() {
        AllPlotTests.runAllTests(skiaCanvasPeer, imageComparer)
    }

    @Test
    fun runSinglePlotTest() {
        val testSuit = PlotInteractivityTest(skiaCanvasPeer, imageComparer)
        testSuit.assertTest(testSuit::plot_interactivity_nestedComposite_tooltip)
    }

    companion object {
        private val SKIA_PLOT_COMPARISON_PROFILE = ComparisonProfile(tol = 2, maxShift = 2, allowedDiffPixelRatio = 0.06)
    }
}
