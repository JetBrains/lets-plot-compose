/*
 * Copyright (c) 2023. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

plugins {
    kotlin("android")
    id("com.android.application")
}

android {
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    namespace = "demo.plot.MinDemo"

    defaultConfig {
        applicationId = "demo.plot.MinDemo"

        minSdk = (findProperty("android.minSdk") as String).toInt()
        targetSdk = (findProperty("android.targetSdk") as String).toInt()

        versionCode = 1
        versionName = "1.0"

        ndk {
            abiFilters += listOf("x86_64", "arm64-v8a")
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        jvmToolchain(11)
    }
}

val letsPlotVersion = extra["letsPlot.version"] as String
val letsPlotKotlinVersion = extra["letsPlotKotlin.version"] as String

dependencies {
    implementation(project(":platf-skia"))
    implementation(project(":demo-plot-shared"))
    implementation("org.jetbrains.lets-plot:lets-plot-kotlin-kernel:${letsPlotKotlinVersion}")
    implementation("org.jetbrains.lets-plot:lets-plot-common:$letsPlotVersion")
    implementation("org.jetbrains.lets-plot:canvas:${letsPlotVersion}")
    implementation("org.jetbrains.lets-plot:plot-raster:$letsPlotVersion")
}
