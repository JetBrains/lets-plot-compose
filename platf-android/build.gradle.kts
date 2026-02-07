/*
 * Copyright (c) 2023. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.util.Properties

plugins {
  alias(libs.plugins.kotlin.multiplatform.android.library)
  alias(libs.plugins.kotlin.multiplatform)
  `maven-publish`
  signing
}

kotlin {
  jvm()

  androidLibrary {
    namespace = "org.jetbrains.letsPlot.android.canvas"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    minSdk = libs.versions.android.minSdk.get().toInt()

    withDeviceTest {
      instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

//    publishing {
//      singleVariant("release")
//    }
  }

  sourceSets {
    androidMain {
      dependencies {
        compileOnly(libs.letsplot.commons)
        compileOnly(libs.letsplot.datamodel)
        compileOnly(libs.letsplot.canvas)
        compileOnly(libs.letsplot.plot.base)
        compileOnly(libs.letsplot.plot.builder)
        compileOnly(libs.letsplot.plot.stem)
        compileOnly(libs.letsplot.plot.raster)
      }
    }

    androidInstrumentedTest {
      dependencies {
        implementation(libs.kotlin.test)
        implementation(libs.androidx.test.junit)
        implementation(libs.androidx.test.espresso.core)

        implementation(libs.assertj.core)
        implementation(libs.letsplot.commons)
        implementation(libs.letsplot.datamodel)
        implementation(libs.letsplot.canvas)
        implementation(libs.letsplot.plot.base)
        implementation(libs.letsplot.plot.builder)
        implementation(libs.letsplot.plot.stem)
        implementation(libs.letsplot.plot.raster)
      }
    }
  }
}


abstract class PullDebugImagesTask : DefaultTask() {
  @get:Inject abstract val execOperations: ExecOperations

  @get:Input abstract val projectDir: Property<File>

  @get:Input abstract val rootProjectDir: Property<File>

  @TaskAction
  fun execute() {
    val destDir = File(projectDir.get(), "build/test-results/")
    destDir.mkdirs()

    // 1. Load local.properties
    val localProperties = Properties()
    val localPropertiesFile = File(rootProjectDir.get(), "local.properties")
    if (localPropertiesFile.exists()) {
      FileInputStream(localPropertiesFile).use { fis -> localProperties.load(fis) }
    }

    // 2. Get sdk.dir from local.properties
    val sdkDir =
        localProperties.getProperty("sdk.dir")
            ?: System.getenv("ANDROID_HOME")
            ?: System.getProperty("android.home")
    if (sdkDir == null) {
      throw GradleException(
          "sdk.dir not found in local.properties and ANDROID_HOME or android.home not set"
      )
    }

    // 3. Construct adb executable path
    val adbPath = "$sdkDir/platform-tools/adb"
    val adbFile = File(adbPath)
    if (!adbFile.exists()) {
      throw GradleException("adb not found at $adbPath")
    }
    val adbExecutable = adbFile.absolutePath

    // Get the list of connected devices using adb devices
    val adbDevicesOutput = ByteArrayOutputStream()
    execOperations.exec {
      commandLine(adbExecutable, "devices", "-l")
      standardOutput = adbDevicesOutput
      isIgnoreExitValue = true
    }

    val devicesOutput = adbDevicesOutput.toString()

    val devices =
        devicesOutput
            .reader()
            .readLines()
            .drop(1) // Skip the header line
            .filter { it.isNotBlank() && !it.startsWith("* daemon") } // Remove empty lines
            .map { it.split("\\s+".toRegex())[0] }

    if (devices.isEmpty()) {
      println("No connected Android devices found.")
      return
    }

    devices.forEach { deviceSerial ->
      println("Pulling images from device: $deviceSerial")

      // The directory on device to pull from
      val devicePicturesDir =
          "/storage/emulated/0/Android/data/org.jetbrains.letsPlot.android.canvas.test/files/Pictures/"
      // The local directory to initially pull the images to
      val tempLocalDir = File(destDir, "temp_pictures")
      if (tempLocalDir.exists()) {
        tempLocalDir.deleteRecursively()
      }
      tempLocalDir.mkdirs()

      // Pull the images from the device to the temporary directory
      execOperations.exec {
        commandLine(
            adbExecutable,
            "-s",
            deviceSerial,
            "pull",
            devicePicturesDir,
            tempLocalDir.absolutePath,
        )
      }

      val tempPicturesDir = File(tempLocalDir, "Pictures")
      val diffImagesDir = File(destDir, "/diff_images/")
      if (diffImagesDir.exists()) {
        diffImagesDir.deleteRecursively()
      }
      diffImagesDir.mkdirs()

      // Move files from temporary dir to destination dir
      tempPicturesDir.listFiles()?.forEach { file -> file.copyTo(File(diffImagesDir, file.name)) }
      // Delete temporary dir
      tempLocalDir.deleteRecursively()
    }
  }
}

tasks.register<PullDebugImagesTask>("pullDebugImages") {
  projectDir.set(project.projectDir)
  rootProjectDir.set(project.rootProject.projectDir)
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
              url.set(
                  "https://raw.githubusercontent.com/JetBrains/lets-plot-compose/master/LICENSE"
              )
            }
          }
          developers {
            developer {
              id.set("jetbrains")
              name.set("JetBrains")
              email.set("lets-plot@jetbrains.com")
            }
          }
          scm { url.set("https://github.com/JetBrains/lets-plot-compose") }
        }
      }
    }

    repositories {
      mavenLocal { url = uri("$rootDir/.maven-publish-dev-repo") }
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
