plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.kotlin.compose.multiplatform)
}

dependencies {
    implementation(compose.desktop.currentOs)
    compileOnly(compose.ui)

    implementation(libs.letsplot.common)

    implementation(projects.letsPlotCompose)
    implementation(projects.demo.svg.shared)
}
