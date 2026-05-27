/*
 * Copyright (c) 2026 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.visualtesting

import org.jetbrains.letsPlot.android.NotoFontManager
import org.jetbrains.letsPlot.android.canvas.AndroidCanvasPeer
import org.jetbrains.letsPlot.visualtesting.plot.AllPlotTests
import org.junit.Test

class AndroidAllPlotTests {
    @Test
    fun runAllTests() {
        val canvasPeer = AndroidCanvasPeer(fontManager = NotoFontManager.INSTANCE)
        val bitmapIO = AndroidBitmapIO(
            expectedImagesDir = "expected-images",
            subdir = "/plot",
            desktopReportDir = AndroidVisualTestConfig.desktopReportDir,
            deviceOutputDir = AndroidVisualTestConfig.deviceOutputDir
        )
        val imageComparer = ImageComparer(canvasPeer, bitmapIO)

        AllPlotTests.runAllTests(canvasPeer, imageComparer)
    }
}
