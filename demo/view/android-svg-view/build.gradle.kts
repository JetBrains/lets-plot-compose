/*
 * Copyright (c) 2023. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    namespace = "demo.svg.view"

    defaultConfig {
        applicationId = "demo.svg.view"

        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.compileSdk.get().toInt()

        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
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
    implementation(projects.platfAndroid)
    implementation(projects.demo.svg.shared)
    implementation(libs.letsplot.common)
    implementation(libs.letsplot.canvas)
    implementation(libs.letsplot.plot.raster)
}
