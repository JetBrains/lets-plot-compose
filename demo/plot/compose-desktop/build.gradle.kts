plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.kotlin.compose.multiplatform)
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.components.resources)

    implementation(libs.letsplot.kotlin.kernel)
    implementation(libs.letsplot.common)
    implementation(libs.letsplot.canvas)
    implementation(libs.letsplot.plot.raster)

    implementation(projects.letsPlotCompose)
    implementation(projects.demo.plot.shared)

    implementation(libs.slf4j.simple)  // Enable logging to console
}
