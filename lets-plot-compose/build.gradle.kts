/*
 * Copyright (c) 2026. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    id("com.android.library")
    id("org.jetbrains.compose")
    `maven-publish`
    signing
}

val androidComposeBom = extra["androidx.compose.bom"] as String
val letsPlotVersion = extra["letsPlot.version"] as String
val letsPlotKotlinVersion = extra["letsPlotKotlin.version"] as String
val kotlinxCoroutinesVersion = extra["kotlinx.coroutines.version"] as String
val kotlinxDatetimeVersion = extra["kotlinx.datetime.version"] as String
val kotlinxBrowserVersion = extra["kotlinx.browser.version"] as String
val kotlinLoggingVersion = extra["kotlinLogging.version"] as String

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

    androidTarget {
        publishLibraryVariants("release")
    }

    wasmJs {
        //outputModuleName = "lets-plot-compose"
        browser()
    }

    sourceSets {
        named("commonMain") {
            dependencies {
                compileOnly(compose.runtime)
                compileOnly(compose.ui)
                compileOnly(compose.foundation)

                compileOnly("org.jetbrains.lets-plot:lets-plot-kotlin:$letsPlotKotlinVersion")
                compileOnly("org.jetbrains.lets-plot:lets-plot-common:$letsPlotVersion")

                api("io.github.oshai:kotlin-logging:$kotlinLoggingVersion")
            }
        }

        named("desktopMain") {
            dependencies {
                compileOnly(compose.desktop.currentOs)
                compileOnly(compose.components.resources)
            }
        }

        named("desktopTest") {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(compose.components.resources)
                implementation("org.jetbrains.lets-plot:lets-plot-kotlin-kernel:$letsPlotKotlinVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinxDatetimeVersion")
                implementation("org.jetbrains.lets-plot:lets-plot-common:$letsPlotVersion")
                implementation("org.jetbrains.lets-plot:visual-testing:$letsPlotVersion")
                implementation(kotlin("test"))
            }
        }

        named("androidMain") {
            dependencies {
                implementation(project.dependencies.platform("androidx.compose:compose-bom:$androidComposeBom"))
                implementation("androidx.compose.ui:ui")
                implementation("androidx.compose.ui:ui-graphics")
                api(project(":platf-android"))
            }
        }

        wasmJsMain {
            dependencies {
                implementation("org.jetbrains.lets-plot:lets-plot-kotlin:$letsPlotKotlinVersion")
                implementation("org.jetbrains.lets-plot:lets-plot-common:$letsPlotVersion")

                implementation("org.jetbrains.kotlinx:kotlinx-browser:$kotlinxBrowserVersion")
            }
        }

    }
}

android {
    namespace = "org.jetbrains.letsPlot.compose"

    compileSdk = (findProperty("android.compileSdk") as String).toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    defaultConfig {
        minSdk = (findProperty("android.minSdk") as String).toInt()
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false // true - error: when compiling demo cant resolve classes
//            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
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


///////////////////////////////////////////////
//  Publishing
///////////////////////////////////////////////

afterEvaluate {
    publishing {
        publications.forEach { pub ->
            with(pub as MavenPublication) {
                artifact(tasks.jarJavaDocs)

                pom {
                    name.set("Lets-Plot Compose Frontend")
                    description.set("Compose frontend for Lets-Plot multiplatform plotting library.")
                    url.set("https://github.com/JetBrains/lets-plot-compose")
                    licenses {
                        license {
                            name.set("MIT")
                            url.set("https://raw.githubusercontent.com/JetBrains/lets-plot-compose/master/LICENSE")
                        }
                    }
                    developers {
                        developer {
                            id.set("jetbrains")
                            name.set("JetBrains")
                            email.set("lets-plot@jetbrains.com")
                        }
                    }
                    scm {
                        url.set("https://github.com/JetBrains/lets-plot-compose")
                    }
                }
            }
        }

        repositories {
            mavenLocal {
                url = uri("$rootDir/.maven-publish-dev-repo")
            }
            maven {
                // For SNAPSHOT publication use separate URL and credentials:
                if (version.toString().endsWith("-SNAPSHOT")) {
                    url = uri(rootProject.project.extra["mavenSnapshotPublishUrl"].toString())

                    credentials {
                        username = rootProject.project.extra["sonatypeUsername"].toString()
                        password = rootProject.project.extra["sonatypePassword"].toString()
                    }
                } else {
                    url = uri(rootProject.project.extra["mavenReleasePublishUrl"].toString())
                }
            }
        }
    }
}

signing {
    if (!(project.version as String).contains("SNAPSHOT")) {
        sign(publishing.publications)
    }
}
