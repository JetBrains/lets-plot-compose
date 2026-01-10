/*
 * Copyright (c) 2023. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

@file:Suppress("UnstableApiUsage")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "lets-plot-compose-root"

pluginManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
  }
}

dependencyResolutionManagement {
  repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS

  repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://central.sonatype.com/repository/maven-snapshots/")
    mavenLocal()

    // Load local.properties for custom maven repos
    val localPropertiesFile = rootDir.resolve("local.properties")
    if (localPropertiesFile.exists()) {
      val localProps = java.util.Properties().apply { load(localPropertiesFile.inputStream()) }
      localProps["maven.repo.local"]?.let { repos ->
        (repos as String).split(",").forEach { repo ->
          maven { url = uri(repo.trim()) }
        }
      }
    }
  }
}

include(
    ":lets-plot-compose",
    ":platf-android",
    ":platf-skia",

    // =========================================
    // Plot Demos
    // =========================================
    ":demo:plot:shared",
    ":demo:plot:compose-desktop",
    ":demo:plot:swing",
    ":demo:plot:compose-android-min",
    ":demo:plot:compose-android-median",
    ":demo:plot:compose-android-redraw",

    // =========================================
    // Pure SVG Rendering - Internal for testing
    // =========================================
    ":demo:svg:shared",
    ":demo:svg:compose-desktop",
    ":demo:svg:swing",

    // =========================================
    // SVG View Rendering - Internal for testing
    // =========================================
    ":demo:view:android-svg-view",
    ":demo:view:android-plot-view"
)
