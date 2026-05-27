/*
 * Copyright (c) 2026 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.visualtesting

import org.jetbrains.letsPlot.android.NotoFontManager
import org.jetbrains.letsPlot.android.canvas.AndroidCanvasPeer
import org.jetbrains.letsPlot.visualtesting.canvas.AllCanvasTests
import org.junit.Test


class AndroidAllCanvasTests {
    @Test
    fun runAllTests() {
        val notoFontManager = NotoFontManager.INSTANCE
        val canvasPeer = AndroidCanvasPeer(fontManager = notoFontManager)
        val bitmapIO = AndroidBitmapIO(
            expectedImagesDir = "expected-images",
            subdir = "/canvas",
            desktopReportDir = AndroidVisualTestConfig.desktopReportDir,
            deviceOutputDir = AndroidVisualTestConfig.deviceOutputDir
        )
        val imageComparer = ImageComparer(canvasPeer, bitmapIO)
        AllCanvasTests.runAllTests(canvasPeer, imageComparer)
    }
}
