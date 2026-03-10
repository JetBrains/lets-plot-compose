package org.jetbrains.letsPlot.visualtesting.canvas

import org.jetbrains.letsPlot.android.NotoFontManager
import org.jetbrains.letsPlot.android.canvas.AndroidCanvasPeer
import org.jetbrains.letsPlot.visualtesting.AndroidBitmapIO
import org.jetbrains.letsPlot.visualtesting.ImageComparer
import org.junit.Test


class AndroidCanvasTck {
    @Test
    fun runAllTests() {
        val notoFontManager = NotoFontManager.INSTANCE
        val canvasPeer = AndroidCanvasPeer(fontManager = notoFontManager)
        val bitmapIO = AndroidBitmapIO(
            expectedImagesDir = "expected-images",
            subdir = "/canvas"
        )
        val imageComparer = ImageComparer(canvasPeer, bitmapIO)
        AllCanvasTests.runAllTests(canvasPeer, imageComparer)
    }
}
