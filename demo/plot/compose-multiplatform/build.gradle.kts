/*
 * Copyright (c) 2026. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

/*
 * Copyright (c) 2025 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
    id("com.android.application")
}

val composeVersion = extra["compose.version"] as String
val letsPlotVersion = extra["letsPlot.version"] as String
val letsPlotKotlinVersion = extra["letsPlotKotlin.version"] as String
val activityComposeVersion = findProperty("androidx.activity.compose") as String

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }

    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }

    wasmJs() {
        outputModuleName = "composeMultiplatformApp"
        browser {
            commonWebpackConfig {
                outputFileName = "composeMultiplatformApp.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.compose.runtime:runtime:$composeVersion")
                implementation("org.jetbrains.compose.foundation:foundation:$composeVersion")
                implementation("org.jetbrains.compose.material:material:$composeVersion")
                implementation("org.jetbrains.compose.ui:ui:$composeVersion")

                implementation("org.slf4j:slf4j-simple:2.0.17")

                // Lets-Plot Compose UI
                implementation(project(":lets-plot-compose"))

                // Lets-Plot Kotlin API
                implementation("org.jetbrains.lets-plot:lets-plot-kotlin:${letsPlotKotlinVersion}")

                // Lets-Plot Multiplatform
                implementation("org.jetbrains.lets-plot:lets-plot-common:$letsPlotVersion")
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("androidx.activity:activity-compose:$activityComposeVersion")

                // Android logging
                implementation("com.github.tony19:logback-android:3.0.0")
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("org.jetbrains.compose.components:components-resources:$composeVersion")
            }
        }

    }
}

android {
    namespace = "demo.letsPlot.composeMultiplatform"
    compileSdk = (findProperty("android.compileSdk") as String).toInt()

    defaultConfig {
        applicationId = "demo.letsPlot.composeMultiplatform"
        minSdk = (findProperty("android.minSdk") as String).toInt()
        targetSdk = (findProperty("android.targetSdk") as String).toInt()
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

compose.desktop {
    application {
        mainClass = "demo.letsPlot.composeMultiplatform.MainKt"

        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb
            )
            packageName = "lets-plot-compose-multiplatform-demo"
            packageVersion = "1.0.0"
        }
    }
}