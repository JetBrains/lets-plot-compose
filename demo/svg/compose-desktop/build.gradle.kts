plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.kotlin.compose.multiplatform)
}

dependencies {
    implementation(compose.desktop.currentOs)
    
    implementation(libs.letsplot.common)

    implementation(projects.letsPlotCompose)
    implementation(projects.demo.svg.shared)
}
