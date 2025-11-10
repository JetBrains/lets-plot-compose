/*
 * Copyright (c) 2023. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

plugins {
    kotlin("android")
    id("com.android.application")
    kotlin("plugin.compose")
}


android {
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    namespace = "demo.plot.CanvasDemo"

    buildFeatures {
        compose = true
    }

    defaultConfig {
        applicationId = "demo.plot.CanvasDemo"

        minSdk = (findProperty("android.minSdk") as String).toInt()
        targetSdk = (findProperty("android.targetSdk") as String).toInt()

        versionCode = 1
        versionName = "1.0"
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

val androidComposeBom = extra["androidx.compose.bom"] as String
val androidxActivityCompose = extra["androidx.activity.compose"] as String
val letsPlotVersion = extra["letsPlot.version"] as String
val letsPlotKotlinVersion = extra["letsPlotKotlin.version"] as String

dependencies {
    implementation(platform("androidx.compose:compose-bom:$androidComposeBom"))
    implementation("androidx.activity:activity-compose:$androidxActivityCompose")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")

    implementation("org.jetbrains.lets-plot:lets-plot-kotlin-kernel:$letsPlotKotlinVersion")
    implementation("org.jetbrains.lets-plot:lets-plot-common:$letsPlotVersion")
    implementation("org.jetbrains.lets-plot:plot-stem:$letsPlotVersion")
    implementation("org.jetbrains.lets-plot:canvas:$letsPlotVersion")
    implementation("org.jetbrains.lets-plot:plot-raster:$letsPlotVersion")

    implementation(project(":lets-plot-compose"))
    implementation(project(":demo-plot-shared"))

}
