pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    plugins {
        val kotlinVersion = extra["kotlin.version"] as String
        val composeVersion = extra["compose.version"] as String
        val agpVersion = extra["agp.version"] as String
        val nexusStagingVersion = extra["nexusStaging.version"] as String
        val nexusPublishVersion = extra["nexusPublish.version"] as String

        kotlin("jvm").version(kotlinVersion)
        kotlin("multiplatform").version(kotlinVersion)
        kotlin("plugin.compose").version(kotlinVersion)
        id("org.jetbrains.compose").version(composeVersion)

        kotlin("android").version(kotlinVersion)
        id("com.android.application").version(agpVersion)
        id("com.android.library").version(agpVersion)

        id("io.codearte.nexus-staging") version nexusStagingVersion
        id("io.github.gradle-nexus.publish-plugin") version nexusPublishVersion
    }
}

include("lets-plot-compose")
include("platf-android")
include("platf-skia")

// =============================
//          Plot Demos
// =============================

include("demo-plot-shared")
project(":demo-plot-shared").projectDir = File("./demo/plot/shared")

include("demo-plot-compose-desktop")
project(":demo-plot-compose-desktop").projectDir = File("./demo/plot/compose-desktop")

include("demo-plot-swing")
project(":demo-plot-swing").projectDir = File("./demo/plot/swing")

include("demo-plot-compose-android-min")
project(":demo-plot-compose-android-min").projectDir = File("./demo/plot/compose-android-min")

include("demo-plot-compose-android-median")
project(":demo-plot-compose-android-median").projectDir = File("./demo/plot/compose-android-median")

include("demo-plot-compose-android-redraw")
project(":demo-plot-compose-android-redraw").projectDir = File("./demo/plot/compose-android-redraw")


// =============================
// Pure SVG Rendering
// Internal - for testing.
// =============================

include("demo-svg-shared")
project(":demo-svg-shared").projectDir = File("./demo/svg/shared")

include("demo-svg-compose-desktop")
project(":demo-svg-compose-desktop").projectDir = File("./demo/svg/compose-desktop")

include("demo-svg-swing")
project(":demo-svg-swing").projectDir = File("./demo/svg/swing")

// =============================
// SVG View Rendering
// Internal - for testing.
// =============================

include("demo-svg-view-android")
project(":demo-svg-view-android").projectDir = File("./demo/view/android-svg-view")

include("demo-plot-view-android")
project(":demo-plot-view-android").projectDir = File("./demo/view/android-plot-view")
