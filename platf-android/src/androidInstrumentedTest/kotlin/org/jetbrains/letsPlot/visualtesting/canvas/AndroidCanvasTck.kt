package org.jetbrains.letsPlot.visualtesting.canvas

import org.jetbrains.letsPlot.android.canvas.AndroidCanvasPeer
import org.jetbrains.letsPlot.visualtesting.AndroidBitmapIO
import org.jetbrains.letsPlot.visualtesting.ImageComparer
import org.junit.Test


class AndroidCanvasTck {
    @Test
    fun runAllTests() {
        val canvasPeer = AndroidCanvasPeer()
        val bitmapIO = AndroidBitmapIO(subdir = "/canvas")
        val imageComparer = ImageComparer(canvasPeer, bitmapIO)
        AllCanvasTests.runAllTests(canvasPeer, imageComparer)
    }
}
