/*
 * Copyright (c) 2026. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
}

val letsPlotVersion = extra["letsPlot.version"] as String
val letsPlotKotlinVersion = extra["letsPlotKotlin.version"] as String

kotlin {
    wasmJs {
        outputModuleName = "composeWasmJsApp"
        browser {
            commonWebpackConfig {
                outputFileName = "composeWasmJsApp.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.ui)
                implementation(compose.components.resources)

                implementation("org.jetbrains.lets-plot:lets-plot-kotlin:$letsPlotKotlinVersion")
                implementation("org.jetbrains.lets-plot:lets-plot-common:${letsPlotVersion}")

                implementation(project(":lets-plot-compose"))
                implementation(project(":demo-plot-shared"))

                //implementation("org.slf4j:slf4j-simple:2.0.9")  // Enable logging to console
            }
        }
        wasmJsMain {
            dependencies {

            }
        }
    }
}


