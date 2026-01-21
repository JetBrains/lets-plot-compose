/*
 * Copyright (c) 2023. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.kotlin.logging.jvm)

    compileOnly(libs.letsplot.commons)
    compileOnly(libs.letsplot.datamodel)

    testImplementation(libs.kotlin.test)
}
