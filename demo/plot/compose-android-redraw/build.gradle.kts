/*
 * Copyright (c) 2023. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose.compiler)
}

android {
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    namespace = "demo.letsPlot"

    buildFeatures {
        compose = true
    }

    defaultConfig {
        applicationId = "demo.letsPlot.composeMinDemo"

        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.compileSdk.get().toInt()

        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("debug")
        }
        debug {
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlin {
        jvmToolchain(21)
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.activity.compose)

    implementation(libs.letsplot.kotlin.kernel)
    implementation(libs.letsplot.common)
    implementation(libs.letsplot.canvas)
    implementation(libs.letsplot.plot.raster)

    implementation(projects.letsPlotCompose)
    implementation(projects.demo.plot.shared)
}
