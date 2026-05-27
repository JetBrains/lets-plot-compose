/*
 * Copyright (c) 2026 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.visualtesting

import androidx.test.platform.app.InstrumentationRegistry

object AndroidVisualTestConfig {
    private const val ARG_DESKTOP_REPORT_DIR = "visualTest.desktopReportDir"
    private const val ARG_DEVICE_OUTPUT_DIR = "visualTest.deviceOutputDir"

    private const val DEFAULT_DESKTOP_REPORT_DIR = "platf-android/build/reports"
    private const val DEFAULT_DEVICE_OUTPUT_DIR = "/sdcard/Download/VisualTestResults"

    val desktopReportDir: String = instrumentationArgument(ARG_DESKTOP_REPORT_DIR) ?: DEFAULT_DESKTOP_REPORT_DIR
    val deviceOutputDir: String = instrumentationArgument(ARG_DEVICE_OUTPUT_DIR) ?: DEFAULT_DEVICE_OUTPUT_DIR

    private fun instrumentationArgument(name: String): String? {
        return InstrumentationRegistry.getArguments().getString(name)?.takeIf { it.isNotBlank() }
    }
}
