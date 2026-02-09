/*
 * Copyright (c) 2023. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    `maven-publish`
    signing
}

val letsPlotVersion = extra["letsPlot.version"] as String
val assertjVersion = extra["assertj.version"] as String
val junitVersion = extra["junit.version"] as String
val espressoCoreVersion = extra["espresso.core.version"] as String

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
    }

    androidTarget {
        publishLibraryVariants("release")
    }

    sourceSets {
        named("androidMain") {
            dependencies {
                compileOnly("org.jetbrains.lets-plot:commons:$letsPlotVersion")
                compileOnly("org.jetbrains.lets-plot:datamodel:$letsPlotVersion")
                compileOnly("org.jetbrains.lets-plot:canvas:$letsPlotVersion")
                compileOnly("org.jetbrains.lets-plot:plot-base:$letsPlotVersion")
                compileOnly("org.jetbrains.lets-plot:plot-builder:$letsPlotVersion")
                compileOnly("org.jetbrains.lets-plot:plot-stem:$letsPlotVersion")
                compileOnly("org.jetbrains.lets-plot:plot-raster:$letsPlotVersion")
            }
        }

        named("androidInstrumentedTest") {
            dependencies {
                implementation(kotlin("test"))
                implementation("androidx.test.ext:junit:$junitVersion")
                implementation("androidx.test.espresso:espresso-core:$espressoCoreVersion")

                implementation("org.assertj:assertj-core:$assertjVersion")
                implementation("org.jetbrains.lets-plot:commons:$letsPlotVersion")
                implementation("org.jetbrains.lets-plot:datamodel:$letsPlotVersion")
                implementation("org.jetbrains.lets-plot:canvas:$letsPlotVersion")
                implementation("org.jetbrains.lets-plot:plot-base:$letsPlotVersion")
                implementation("org.jetbrains.lets-plot:plot-builder:$letsPlotVersion")
                implementation("org.jetbrains.lets-plot:plot-stem:$letsPlotVersion")
                implementation("org.jetbrains.lets-plot:plot-raster:$letsPlotVersion")
                //implementation("org.jetbrains.lets-plot:visual-testing:$letsPlotVersion")
            }
        }
    }
}

android {
    namespace = "org.jetbrains.letsPlot.android.canvas"

    compileSdk = (findProperty("android.compileSdk") as String).toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    defaultConfig {
        minSdk = (findProperty("android.minSdk") as String).toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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


tasks.register<Exec>("clearDeviceImages") {
    group = "verification"
    isIgnoreExitValue = true
    val adb = android.adbExecutable.path
    // Clean the GLOBAL folder
    val remoteDir = "/sdcard/Download/VisualTestResults/"
    commandLine(adb, "shell", "rm", "-rf", remoteDir)
}

tasks.register<Exec>("pullTestImages") {
    group = "verification"
    isIgnoreExitValue = true
    val adb = android.adbExecutable.path

    val remoteDir = "/sdcard/Download/VisualTestResults"
    val finalDir = layout.buildDirectory.dir("reports").get().asFile
    // We use a temporary folder for the raw pull
    val tempDir = layout.buildDirectory.dir("tmp/visual_pull_buffer").get().asFile

    doFirst {
        // Ensure both directories exist
        finalDir.mkdirs()
        tempDir.mkdirs()
        println("📥 Pulling images to temp buffer...")
    }

    // 1. Pull the folder into the temp directory
    // This creates: build/tmp/visual_pull_buffer/VisualTestResults/...
    commandLine(adb, "pull", remoteDir, tempDir.absolutePath)

    doLast {
        // 2. Locate the specific folder ADB created
        val pulledSubDir = file("${tempDir.absolutePath}/VisualTestResults")

        if (pulledSubDir.exists()) {
            println("📂 Moving files to final destination...")

            // 3. Use Gradle's safe copy to move just the files, flattening the structure
            copy {
                from(pulledSubDir)
                into(finalDir)
            }

            println("✅ Images saved to: ${finalDir.absolutePath}")
        } else {
            println("⚠️ No images found on device (Test passed or skipped).")
        }

        // 4. Cleanup the temp folder
        tempDir.deleteRecursively()
    }
}

// Keep the hook configuration the same
tasks.configureEach {
    if (name.contains("connected") && name.contains("AndroidTest")) {
        dependsOn("clearDeviceImages")
        finalizedBy("pullTestImages")
    }
}

// 4. AUTOMATION HOOK (Corrected)
// We use 'configureEach' on the generic Task type to avoid importing internal classes.
tasks.configureEach {
    // Check if the task name matches the standard pattern for Android Instrumentation tests
    // e.g., "connectedDebugAndroidTest", "connectedDemoDebugAndroidTest"
    if (name.contains("connected") && name.contains("AndroidTest")) {

        // Step A: Run 'clearDeviceImages' BEFORE the test starts
        //dependsOn("clearDeviceImages")

        // Step B: Run 'pullTestImages' AFTER the test finishes (fail or success)
        finalizedBy("pullTestImages")
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
                    name.set("Lets-Plot Compose - Android")
                    description.set("Android drawing for Lets-Plot Compose plotting library.")
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