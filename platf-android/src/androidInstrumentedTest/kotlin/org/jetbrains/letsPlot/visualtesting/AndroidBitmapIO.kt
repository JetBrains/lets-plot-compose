/*
 * Copyright (c) 2026 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.visualtesting
/*
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import org.jetbrains.letsPlot.commons.values.Bitmap
import java.io.File

object AndroidBitmapIO : ImageComparer.BitmapIO {
    // Hardcoded path relative to the project root.
    // IntelliJ Console will recognize this and make it clickable.
    private const val REPORT_PATH = "/Users/ikupriyanov/Projects/lets-plot-compose/platf-android/build/reports"

    override fun getActualFileReportPath(fileName: String): String {
        return "$REPORT_PATH/$fileName"
    }

    override fun getExpectedFileReportPath(fileName: String): String {
        // Points to the expected image in the reports folder
        // (assuming your test logic copies the "golden" image there on failure)
        return "$REPORT_PATH/$fileName"
    }

    override fun getDiffFileReportPath(fileName: String): String {
        return "$REPORT_PATH/$fileName"
    }

    override fun write(bitmap: Bitmap, fileName: String) {
        // 1. Write to the App's private storage (as before)
        val privatePath = getWriteFilePath(fileName)
        val file = File(privatePath)
        file.parentFile?.mkdirs()

        Log.d("VisualTest", "Writing to private: $privatePath")

        val image = android.graphics.Bitmap.createBitmap(bitmap.width, bitmap.height, android.graphics.Bitmap.Config.ARGB_8888)
        image.setPixels(bitmap.argbInts, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        file.outputStream().use { out ->
            image.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
        }

        // 2. MAGIC STEP: Copy to Global Storage (/sdcard/Download/)
        // This folder persists even if the app is uninstalled/cleared.
        copyToGlobalStorage(fileName, privatePath)    }

    override fun read(fileName: String): Bitmap {
        val stream = javaClass.classLoader?.getResourceAsStream("expected-images/$fileName")

        val expectedBitmap = BitmapFactory.decodeStream(stream)
            ?: error("Failed to read expected image: $stream")

        return Bitmap(
            expectedBitmap.width,
            expectedBitmap.height,
            IntArray(expectedBitmap.width * expectedBitmap.height).also {
                expectedBitmap.getPixels(
                    it,
                    0,
                    expectedBitmap.width,
                    0,
                    0,
                    expectedBitmap.width,
                    expectedBitmap.height
                )
            }
        )
    }

    override fun getReadFilePath(fileName: String): String {
        return fileName
    }

    override fun getWriteFilePath(fileName: String): String {
        return absPath(fileName)
    }

    private fun absPath(filename: String): String {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: error("Failed to get external files directory")

        return File(dir, filename).absolutePath
    }

    private fun copyToGlobalStorage(fileName: String, privatePath: String) {
        // We define a safe global path
        val globalDir = "/sdcard/Download/VisualTestResults"
        val globalPath = "$globalDir/$fileName"

        try {
            // Use UiAutomation to execute a shell command.
            // The Shell has higher permissions than the App and can write to /sdcard/Download
            val instrumentation = InstrumentationRegistry.getInstrumentation()

            // 1. Create the directory
            instrumentation.uiAutomation.executeShellCommand("mkdir -p $globalDir")

            // 2. Copy the file
            // We use 'cp' command from the shell
            instrumentation.uiAutomation.executeShellCommand("cp $privatePath $globalPath")

            Log.d("VisualTest", "✅ SAVED GLOBALLY: $globalPath")
        } catch (e: Exception) {
            Log.e("VisualTest", "❌ Failed to copy to global storage", e)
        }
    }
}
*/