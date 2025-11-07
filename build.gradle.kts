/*
 * Copyright (c) 2023. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */


// okhttp3 added for publishing to the Sonatype Central Repository:
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.util.*

buildscript {
    dependencies {
        classpath("com.squareup.okhttp3:okhttp:4.12.0")
    }
}

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    kotlin("multiplatform").apply(false)
    kotlin("plugin.compose").apply(false)
    kotlin("jvm").apply(false)
    id("org.jetbrains.compose").apply(false)

    kotlin("android").apply(false)
    id("com.android.application").apply(false)
    id("com.android.library").apply(false)

    id("io.codearte.nexus-staging").apply(false)
    id("io.github.gradle-nexus.publish-plugin")
}

val localProps = Properties()
if (project.file("local.properties").exists()) {
    localProps.load(project.file("local.properties").inputStream())
} else {
    error("Couldn't read local.properties")
}

allprojects {
    group = "org.jetbrains.lets-plot"
    version = "3.0.1-SNAPSHOT"
//    version = "0.0.0-SNAPSHOT" // for local publishing only

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
        kotlinOptions {
            jvmTarget = "11"
        }
    }

    tasks.withType<JavaCompile>().all {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }
}

// define the Maven Repository URL. Currently set to a local path for uploading
// artifacts to the Sonatype Central Repository.
val mavenReleasePublishUrl by extra { layout.buildDirectory.dir("maven/artifacts").get().toString() }
// define Maven Snapshot repository URL.
val mavenSnapshotPublishUrl by extra { "https://central.sonatype.com/repository/maven-snapshots/" }

// define Sonatype Central Repository settings:
val sonatypeUsername by extra { localProps["sonatype.username"] ?: "" }
val sonatypePassword by extra { localProps["sonatype.password"] ?: "" }

val packageMavenArtifacts by tasks.registering(Zip::class) {

    from(mavenReleasePublishUrl)
    archiveFileName.set("${project.name}-artifacts.zip")
    destinationDirectory.set(layout.buildDirectory)
}
val uploadMavenArtifacts by tasks.registering {
    dependsOn(packageMavenArtifacts)

    doLast {
        val uriBase = "https://central.sonatype.com/api/v1/publisher/upload"
        val publishingType = "USER_MANAGED"
        val deploymentName = "${project.name}-$version"
        val uri = "$uriBase?name=$deploymentName&publishingType=$publishingType"

        val userName = sonatypeUsername as String
        val password = sonatypePassword as String
        val base64Auth = Base64.getEncoder().encode("$userName:$password".toByteArray()).toString(Charsets.UTF_8)
        val bundleFile = packageMavenArtifacts.get().archiveFile.get().asFile

        println("Sending request to $uri...")

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(uri)
            .header("Authorization", "Bearer $base64Auth")
            .post(
                MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("bundle", bundleFile.name, bundleFile.asRequestBody())
                    .build()
            )
            .build()

        client.newCall(request).execute().use { response ->
            val statusCode = response.code
            println("Upload status code: $statusCode")
            println("Upload result: ${response.body!!.string()}")
            if (statusCode != 201) {
                error("Upload error to Central repository. Status code $statusCode.")
            }
        }
    }
}


subprojects {
    repositories {
        mavenCentral()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")

        // Repositories where other projects publish their artifacts locally to.
        localProps["maven.repo.local"]?.let {
            (it as String).split(",").forEach { repo ->
                mavenLocal {
                    url = uri(repo)
                }
            }
        }

        // SNAPSHOTS
        maven(url = mavenSnapshotPublishUrl)

        mavenLocal()
    }

    val jarJavaDocs by tasks.creating(Jar::class) {
        archiveClassifier.set("javadoc")
        group = "lets plot"
        from("$rootDir/README.md")
    }

    // ------------------------------------------
    // Workaround for the error when signing published artifacts.
    // It seems to appear after switching to Gradle 8.3
    // For details see: https://github.com/gradle/gradle/issues/26091 :
    // Publishing a KMP project with signing fails with "Task ... uses this output of task ... without declaring an explicit or implicit dependency"
    // https://github.com/gradle/gradle/issues/26091
    tasks.withType<AbstractPublishToMaven>().configureEach {
        val signingTasks = tasks.withType<Sign>()
        mustRunAfter(signingTasks)
    }
}
