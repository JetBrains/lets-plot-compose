/*
 * Copyright (c) 2023. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

plugins {
    alias(libs.plugins.kotlin.multiplatform.android.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.kotlin.compose.multiplatform)
    `maven-publish`
    signing
}

kotlin {
    jvm("desktop")

    androidLibrary {
        namespace = "org.jetbrains.letsPlot.compose"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    sourceSets {
        commonMain {
            dependencies {
                compileOnly(compose.runtime)
                compileOnly(compose.ui)
                compileOnly(compose.foundation)

                compileOnly(libs.letsplot.kotlin.kernel)
                compileOnly(libs.letsplot.common)
                compileOnly(libs.letsplot.plot.raster)
                compileOnly(libs.letsplot.canvas)
                implementation(libs.kotlinx.datetime)
            }
        }

        val desktopMain by getting {
            dependencies {
                compileOnly(compose.runtime)
                compileOnly(compose.ui)
                compileOnly(compose.desktop.currentOs)
                compileOnly(compose.components.resources)
                compileOnly(libs.skiko)
                api(projects.platfSkia)
                compileOnly(libs.kotlin.logging.jvm)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(project.dependencies.platform(libs.androidx.compose.bom))
                implementation(libs.androidx.compose.ui)
                implementation(libs.androidx.compose.ui.graphics)
                api(projects.platfAndroid)
                compileOnly(libs.letsplot.plot.raster)
                compileOnly(libs.letsplot.canvas)
            }
        }
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
                url = rootDir.resolve(".maven-publish-dev-repo").toURI()
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
