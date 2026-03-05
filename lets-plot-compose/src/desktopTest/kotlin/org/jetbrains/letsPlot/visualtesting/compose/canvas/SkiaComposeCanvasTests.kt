package org.jetbrains.letsPlot.visualtesting.compose.canvas

import org.jetbrains.letsPlot.compose.canvas.SkiaCanvasPeer
import org.jetbrains.letsPlot.visualtesting.AwtBitmapIO
import org.jetbrains.letsPlot.visualtesting.ImageComparer
import org.jetbrains.letsPlot.visualtesting.canvas.AllCanvasTests
import kotlin.test.Ignore
import kotlin.test.Test


class SkiaComposeCanvasTests {

    @Ignore
    @Test
    fun runAllCanvasTests() {
        val awtBitmapIO = AwtBitmapIO(
            expectedImagesDir = "/src/desktopTest/resources/expected-images",
            subdir = "/canvas"
        )
        val canvasPeer = SkiaCanvasPeer()
        val imageComparer = ImageComparer(canvasPeer, awtBitmapIO, silent = true)

        AllCanvasTests.runAllTests(canvasPeer, imageComparer)
    }
}
