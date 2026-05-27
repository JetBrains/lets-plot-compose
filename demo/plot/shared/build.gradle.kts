@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

/*
 * Copyright (c) 2023. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

plugins {
    kotlin("multiplatform")
}

val letsPlotVersion = extra["letsPlot.version"] as String
val letsPlotKotlinVersion = extra["letsPlotKotlin.version"] as String

kotlin {
    jvm() // for Android
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain {
            dependencies {
                compileOnly("org.jetbrains.lets-plot:lets-plot-kotlin:$letsPlotKotlinVersion")
                compileOnly("org.jetbrains.lets-plot:lets-plot-common:$letsPlotVersion")
            }
        }
    }
}
