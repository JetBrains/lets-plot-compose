/*
 * Copyright (c) 2023. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  `maven-publish`
  signing
}

kotlin {
  jvm()

  sourceSets {
    commonMain {
      dependencies {
        compileOnly(libs.skiko)

        compileOnly(libs.letsplot.commons)
        compileOnly(libs.letsplot.datamodel)
        compileOnly(libs.letsplot.plot.base)
        compileOnly(libs.letsplot.plot.stem)
        compileOnly(libs.letsplot.plot.builder)
        compileOnly(libs.letsplot.plot.raster)
        compileOnly(libs.letsplot.canvas)
      }
    }

    jvmMain  { dependencies { compileOnly(libs.kotlin.logging.jvm) } }

    jvmTest {
      dependencies {
        implementation(libs.kotlin.test)
        implementation(libs.assertj.core)
        implementation(libs.skiko)
        implementation(libs.letsplot.commons)
        implementation(libs.letsplot.datamodel)
        implementation(libs.kotlin.logging)
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
          name.set("Lets-Plot Compose - Skia")
          description.set("Skia drawing for Lets-Plot Compose plotting library.")
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
      mavenLocal { url = rootDir.resolve(".maven-publish-dev-repo").toURI() }
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
