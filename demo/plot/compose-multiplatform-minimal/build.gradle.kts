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
    id("com.android.application")
}

val androidComposeBom = extra["androidx.compose.bom"] as String
val androidxActivityCompose = extra["androidx.activity.compose"] as String
val composeVersion = extra["compose.version"] as String
val letsPlotVersion = extra["letsPlot.version"] as String
val letsPlotKotlinVersion = extra["letsPlotKotlin.version"] as String
val slf4jVersion = extra["slf4j.version"] as String

kotlin {
    jvm("desktop") {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
                }
            }
        }
    }

    androidTarget()

    wasmJs {
        outputModuleName = "composeMultiplatformApp"
        browser {
            commonWebpackConfig {
                outputFileName = "composeMultiplatformApp.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation("org.jetbrains.compose.runtime:runtime:$composeVersion")
                implementation("org.jetbrains.compose.foundation:foundation:$composeVersion")
                implementation("org.jetbrains.compose.material:material:$composeVersion")
                implementation("org.jetbrains.compose.ui:ui:$composeVersion")

                implementation("org.jetbrains.lets-plot:lets-plot-kotlin:$letsPlotKotlinVersion")
                implementation("org.jetbrains.lets-plot:commons:$letsPlotVersion")
                implementation("org.jetbrains.lets-plot:canvas:$letsPlotVersion")
                implementation("org.jetbrains.lets-plot:datamodel:$letsPlotVersion")
                implementation("org.jetbrains.lets-plot:plot-base:$letsPlotVersion")
                implementation("org.jetbrains.lets-plot:plot-builder:$letsPlotVersion")
                implementation("org.jetbrains.lets-plot:plot-stem:$letsPlotVersion")
                implementation("org.jetbrains.lets-plot:plot-raster:$letsPlotVersion")

                implementation(project(":lets-plot-compose"))
                implementation(project(":demo-plot-shared"))

                implementation("org.slf4j:slf4j-simple:${slf4jVersion}")
            }
        }

        named("desktopMain") {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(compose.components.resources)
            }
        }

        named("androidMain") {
            dependencies {
                implementation(project.dependencies.platform("androidx.compose:compose-bom:$androidComposeBom"))
                implementation("androidx.activity:activity-compose:$androidxActivityCompose")
                implementation("androidx.compose.material3:material3")
            }
        }

        wasmJsMain {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-browser:0.5.0")
            }
        }
    }
}

android {
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    namespace = "demo.plot.multiplatform"

    buildFeatures {
        compose = true
    }

    defaultConfig {
        applicationId = "demo.plot.multiplatform"
        minSdk = (findProperty("android.minSdk") as String).toInt()
        targetSdk = (findProperty("android.targetSdk") as String).toInt()

        versionCode = 1
        versionName = "1.0"
    }

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")

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

compose.desktop {
    application {
        mainClass = "demo.plot.multiplatform.MainKt"
    }
}
