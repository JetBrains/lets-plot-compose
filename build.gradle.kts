/*
 * Copyright (c) 2023. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

@file:Suppress("UnstableApiUsage")

import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import java.util.Base64
import java.util.Properties

buildscript { dependencies { classpath(libs.okhttp) } }

plugins {
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.kotlin.compose.compiler) apply false
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.kotlin.multiplatform.android.library) apply false
  alias(libs.plugins.nexus.staging) apply false
  alias(libs.plugins.nexus.publish)
}

// =============================
//     Properties & Config
// =============================

val localProps =
    Properties().apply {
      val localPropertiesFile = rootProject.file("local.properties")
      if (localPropertiesFile.exists()) {
        load(localPropertiesFile.inputStream())
      }
    }

val javaVersion: String = libs.versions.java.get()
val javaLanguageVersion: JavaLanguageVersion = JavaLanguageVersion.of(javaVersion)

// =============================
//     Maven Publishing Config
// =============================

val mavenReleasePublishUrl by extra {
  layout.buildDirectory.dir("maven/artifacts").get().toString()
}
val mavenSnapshotPublishUrl by extra { "https://central.sonatype.com/repository/maven-snapshots/" }
val sonatypeUsername by extra { localProps["sonatype.username"] ?: "" }
val sonatypePassword by extra { localProps["sonatype.password"] ?: "" }

// =============================
//     All Projects Config
// =============================

allprojects {
  group = "org.jetbrains.lets-plot"
  version = "3.0.3-SNAPSHOT"
}

// =============================
//     Subprojects Config
// =============================

subprojects {
  // Kotlin Configuration
  plugins.withType<KotlinBasePlugin> {
    extensions.configure<KotlinProjectExtension> {
      jvmToolchain { languageVersion.set(javaLanguageVersion) }
    }

    tasks.withType<KotlinCompilationTask<*>>().configureEach {
      compilerOptions {
        apiVersion.set(KotlinVersion.KOTLIN_2_2)
        languageVersion.set(KotlinVersion.KOTLIN_2_2)
        allWarningsAsErrors.set(false)
        optIn.addAll("kotlin.RequiresOptIn", "kotlin.ExperimentalStdlibApi")
        freeCompilerArgs.addAll("-Xjsr305=strict")
      }
    }
  }

  // Java Configuration
  plugins.withType<JavaPlugin> {
    tasks.withType<JavaCompile>().configureEach {
      sourceCompatibility = JavaVersion.toVersion(javaVersion).majorVersion
      targetCompatibility = JavaVersion.toVersion(javaVersion).majorVersion

      options.apply {
        encoding = Charsets.UTF_8.name()
        isFork = true
        isIncremental = true
      }
    }
  }

  // Javadoc JAR for publishing
  val jarJavaDocs by
      tasks.registering(Jar::class) {
        archiveClassifier.set("javadoc")
        group = "publishing"
        from(rootProject.file("README.md"))
      }

  // Workaround for signing issue: https://github.com/gradle/gradle/issues/26091
  tasks.withType<AbstractPublishToMaven>().configureEach { mustRunAfter(tasks.withType<Sign>()) }
}

// =============================
//     Publishing Tasks
// =============================

val packageMavenArtifacts by
    tasks.registering(Zip::class) {
      group = "publishing"
      description = "Packages Maven artifacts for upload to Central Repository"
      from(mavenReleasePublishUrl)
      archiveFileName.set("${rootProject.name}-artifacts.zip")
      destinationDirectory.set(layout.buildDirectory)
    }

val uploadMavenArtifacts by
    tasks.registering {
      group = "publishing"
      description = "Uploads Maven artifacts to Sonatype Central Repository"
      dependsOn(packageMavenArtifacts)

      doLast {
        val uploadUrl = buildString {
          append("https://central.sonatype.com/api/v1/publisher/upload")
          append("?name=${rootProject.name}-$version")
          append("&publishingType=USER_MANAGED")
        }

        val credentials = "$sonatypeUsername:$sonatypePassword"
        val base64Auth = Base64.getEncoder().encodeToString(credentials.toByteArray())
        val bundleFile = packageMavenArtifacts.get().archiveFile.get().asFile

        logger.lifecycle("Uploading to: $uploadUrl")

        val request =
            Request.Builder()
                .url(uploadUrl)
                .header("Authorization", "Bearer $base64Auth")
                .post(
                    MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("bundle", bundleFile.name, bundleFile.asRequestBody())
                        .build()
                )
                .build()

        OkHttpClient().newCall(request).execute().use { response ->
          val statusCode = response.code
          val responseBody = response.body?.string() ?: ""

          logger.lifecycle("Upload status: $statusCode")
          logger.lifecycle("Response: $responseBody")

          check(statusCode == 201) { "Upload failed with status $statusCode: $responseBody" }
        }
      }
    }
