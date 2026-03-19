@file:OptIn(ExperimentalWasmDsl::class)

import com.sun.net.httpserver.HttpServer
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import java.awt.Desktop
import java.net.InetSocketAddress
import java.net.URI

plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
}

val letsPlotVersion = extra["letsPlot.version"] as String
val letsPlotKotlinVersion = extra["letsPlotKotlin.version"] as String

kotlin {
    wasmJs {
        outputModuleName = "composeWebpackApp"
        browser {
            commonWebpackConfig {
                outputFileName = "composeWebpackApp.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.ui)
                implementation(compose.components.resources)

                implementation("org.jetbrains.lets-plot:lets-plot-kotlin:$letsPlotKotlinVersion")
                implementation("org.jetbrains.lets-plot:commons:${letsPlotVersion}")
                implementation("org.jetbrains.lets-plot:canvas:${letsPlotVersion}")
                implementation("org.jetbrains.lets-plot:datamodel:${letsPlotVersion}")
                implementation("org.jetbrains.lets-plot:plot-base:${letsPlotVersion}")
                implementation("org.jetbrains.lets-plot:plot-builder:${letsPlotVersion}")
                implementation("org.jetbrains.lets-plot:plot-stem:${letsPlotVersion}")
                implementation("org.jetbrains.lets-plot:plot-raster:${letsPlotVersion}")
                implementation("org.jetbrains.lets-plot:lets-plot-kotlin:${letsPlotKotlinVersion}")

                implementation(project(":lets-plot-compose"))
                implementation(project(":demo-plot-shared"))

                //implementation("org.slf4j:slf4j-simple:2.0.9")  // Enable logging to console
            }
        }
        wasmJsMain {
            dependencies {

            }
        }
    }
}



// 1. Register the custom task
        tasks.register("runWasmDemo") {
            group = "application"
            description = "Builds the fully bundled WasmJS app, starts a local server, and opens the browser."

            // 2. Depend on the distribution task to guarantee Webpack bundles everything
            dependsOn("wasmJsBrowserDistribution")

            doLast {
                // 3. Find the Webpack output directory
                val distDir = layout.buildDirectory.dir("dist/wasmJs/productionExecutable").get().asFile
                if (!distDir.exists()) {
                    throw GradleException("Distribution directory not found: ${distDir.absolutePath}")
                }

                // 4. Start a lightweight HTTP Server on a random available port
                val server = HttpServer.create(InetSocketAddress("localhost", 0), 0)
                val port = server.address.port

                server.createContext("/") { exchange ->
                    try {
                        val path = if (exchange.requestURI.path == "/") "/index.html" else exchange.requestURI.path
                        val file = File(distDir, path)

                        if (file.exists() && file.isFile) {
                            val bytes = file.readBytes()

                            // CRITICAL: Ensure correct MIME types so the browser accepts the files
                            val mimeType = when (file.extension.lowercase()) {
                                "html" -> "text/html"
                                "js", "mjs" -> "application/javascript"
                                "wasm" -> "application/wasm" // Prevents the Wasm loading error!
                                "css" -> "text/css"
                                else -> "text/plain"
                            }

                            exchange.responseHeaders.add("Content-Type", mimeType)
                            exchange.sendResponseHeaders(200, bytes.size.toLong())
                            exchange.responseBody.use { it.write(bytes) }
                        } else {
                            val notFound = "404 Not Found"
                            exchange.sendResponseHeaders(404, notFound.length.toLong())
                            exchange.responseBody.use { it.write(notFound.toByteArray()) }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        exchange.sendResponseHeaders(500, 0)
                        exchange.responseBody.close()
                    }
                }

                server.start()
                val url = "http://localhost:$port"
                println("\n=======================================================")
                println("🚀 Server started at: $url")
                println("📂 Serving files from: ${distDir.absolutePath}")
                println("🛑 Press Ctrl+C to stop the server.")
                println("=======================================================\n")

                // 5. Open the default web browser automatically
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(URI(url))
                } else {
                    println("Could not open browser automatically. Please navigate to: $url")
                }

                // 6. Keep the task running so the server stays alive
                while (true) {
                    Thread.sleep(1000)
                }
            }
        }