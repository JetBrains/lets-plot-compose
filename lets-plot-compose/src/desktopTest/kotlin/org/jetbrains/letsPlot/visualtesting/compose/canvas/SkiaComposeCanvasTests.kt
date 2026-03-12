package org.jetbrains.letsPlot.visualtesting.compose.canvas

import org.jetbrains.letsPlot.compose.canvas.SkiaCanvasPeer
import org.jetbrains.letsPlot.visualtesting.AwtBitmapIO
import org.jetbrains.letsPlot.visualtesting.ImageComparer
import org.jetbrains.letsPlot.visualtesting.canvas.AllCanvasTests
import org.jetbrains.letsPlot.visualtesting.compose.NotoFontManager
import kotlin.test.Test


class SkiaComposeCanvasTests {
    @Test
    fun runAllCanvasTests() {
        val os = System.getProperty("os.name").lowercase()
        val platf = when {
            os.contains("mac") -> "/macos" // Font thickness differs on macOS, so we use separate expected images.
            else -> ""
        }

        val awtBitmapIO = AwtBitmapIO(
            expectedImagesDir = "/src/desktopTest/resources/expected-images$platf",
            subdir = "/canvas"
        )
        val canvasPeer = SkiaCanvasPeer(NotoFontManager.INSTANCE)
        val imageComparer = ImageComparer(canvasPeer, awtBitmapIO, silent = true)

        AllCanvasTests.runAllTests(canvasPeer, imageComparer)
    }
}
