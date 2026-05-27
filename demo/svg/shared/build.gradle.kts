/*
 * Copyright (c) 2023. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

plugins {
    kotlin("jvm")
}

val kotlinLoggingVersion = extra["kotlinLogging.version"] as String
val letsPlotVersion = extra["letsPlot.version"] as String

dependencies {
    implementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")

    compileOnly("org.jetbrains.lets-plot:lets-plot-common:$letsPlotVersion")

    testImplementation(kotlin("test"))
}
