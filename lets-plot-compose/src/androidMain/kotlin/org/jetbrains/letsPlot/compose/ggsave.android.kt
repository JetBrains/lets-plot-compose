package org.jetbrains.letsPlot.compose

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.os.Build
import android.os.Environment
import org.jetbrains.letsPlot.Figure
import org.jetbrains.letsPlot.android.canvas.AndroidCanvasPeer
import org.jetbrains.letsPlot.commons.encoding.RGBEncoder
import org.jetbrains.letsPlot.commons.geometry.DoubleVector
import org.jetbrains.letsPlot.core.util.MonolithicCommon
import org.jetbrains.letsPlot.core.util.PlotExportCommon.SizeUnit
import org.jetbrains.letsPlot.core.util.PlotExportCommon.computeExportParameters
import org.jetbrains.letsPlot.core.util.PlotHtmlExport
import org.jetbrains.letsPlot.core.util.PlotHtmlHelper.scriptUrl
import org.jetbrains.letsPlot.core.util.PlotSvgExportCommon
import org.jetbrains.letsPlot.export.VersionChecker
import org.jetbrains.letsPlot.intern.toSpec
import org.jetbrains.letsPlot.raster.view.PlotCanvasFigure2
import java.io.File
import java.util.*

/**
 * Thrown by ggsave when it requires a permission that has not been granted by the user.
 */
class MissingStoragePermissionException(message: String) : SecurityException(message)

/**
 * Sanitizes a string to be used as a valid directory name.
 * It replaces any characters that are not letters, numbers, underscores, spaces, dots, or hyphens with an underscore.
 * It also provides a fallback name if the result is blank.
 *
 * @param name The raw string to sanitize.
 * @return A filesystem-safe directory name.
 */
private fun sanitizeDirName(name: String): String {
    // This regex matches one or more characters that are NOT alphanumeric, whitespace, an underscore, a dot, or a hyphen.
    val illegalCharsRegex = "[^\\w\\s.-]+".toRegex()
    return name
        .replace(illegalCharsRegex, "_") // Replace all illegal characters with an underscore
        .trim() // Remove leading/trailing whitespace
        .ifBlank { "LetsPlot" } // If the name is now empty (e.g., was "///"), use a safe fallback.
}


fun ggsave(
    plot: Figure,
    filename: String,
    scale: Number? = null,
    dpi: Number? = null,
    path: String? = null,
    w: Number? = null,
    h: Number? = null,
    unit: SizeUnit? = null,
    context: Context
): String {
    val (ext, file) = prepareOutputFile(filename, path, context)

    val spec: MutableMap<String, Any> = plot.toSpec()
    val plotSize = toDoubleVector(w, h)

    when (ext) {
        "svg" -> {
            val svg = PlotSvgExportCommon.buildSvgImageFromRawSpecs(
                spec,
                plotSize = plotSize,
                rgbEncoder = RGBEncoder.DEFAULT,
                useCssPixelatedImageRendering = true,
                sizeUnit = unit ?: SizeUnit.PX,
            )
            file.createNewFile()
            file.writeText(svg)
        }

        "html", "htm" -> {
            val html = PlotHtmlExport.buildHtmlFromRawSpecs(
                spec,
                scriptUrl = scriptUrl(VersionChecker.letsPlotJsVersion),
                iFrame = true,
                plotSize = plotSize
            )
            file.createNewFile()
            file.writeText(html)
        }

        "png", "jpeg", "jpg" -> {
            val bmp = paintPlot(spec, plotSize, scale, unit, dpi)
            file.createNewFile()
            file.outputStream().use { outStream ->
                when (ext) {
                    "png" -> bmp.compress(CompressFormat.PNG, 90, outStream)
                    "jpeg", "jpg" -> bmp.compress(CompressFormat.JPEG, 90, outStream)
                    else -> throw IllegalArgumentException("Unsupported raster format: \"$ext\".")
                }
            }
        }

        else -> throw java.lang.IllegalArgumentException("Unsupported file extension: \"$ext\".")
    }

    return file.path
}

private fun paintPlot(
    spec: MutableMap<String, Any>,
    plotSize: DoubleVector?,
    scale: Number?,
    unit: SizeUnit?,
    dpi: Number?
): Bitmap {
    @Suppress("NAME_SHADOWING")
    val targetDPI = dpi?.toFiniteDouble()
    val (sizingPolicy, scaleFactor) = computeExportParameters(plotSize, targetDPI, unit, scale)
    val plotFigure = PlotCanvasFigure2()
    plotFigure.update(
        processedSpec = MonolithicCommon.processRawSpecs(spec, frontendOnly = false),
        sizingPolicy = sizingPolicy,
        computationMessagesHandler = {}
    )

    val androidCanvasPeer = AndroidCanvasPeer(scaleFactor)
    val reg = plotFigure.mapToCanvas(androidCanvasPeer)

    val canvas = androidCanvasPeer.createCanvas(plotFigure.size)
    plotFigure.paint(canvas.context2d)
    val bmp = canvas.takeSnapshot().platformBitmap

    reg.dispose()
    androidCanvasPeer.dispose()
    return bmp
}

private fun prepareOutputFile(filename: String, path: String?, context: Context): Pair<String, File> {
    @Suppress("NAME_SHADOWING")
    val filename = filename.trim()
    require(filename.indexOf('.') >= 0) { "File extension is missing: \"$filename\"." }
    val ext = filename.substringAfterLast('.', "").lowercase(Locale.getDefault())
    require(ext.isNotEmpty()) { "Missing file extension: \"$filename\"." }

    // If we are using the default path on an older Android version, we must have permission.
    if (path == null && Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
        val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            throw MissingStoragePermissionException(
                "WRITE_EXTERNAL_STORAGE permission is required on Android 9 and below " +
                        "to save to the public Pictures directory."
            )
        }
    }

    // Path resolution logic
    val dir = path?.let { File(it) } ?: run {
        val appLabel = context.packageManager.getApplicationLabel(context.applicationInfo).toString()
        val sanitizedAppName = sanitizeDirName(appLabel)
        val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        File(picturesDir, sanitizedAppName)
    }

    if (!dir.exists()) {
        dir.mkdirs()
    }

    val file = File(dir, filename)
    return Pair(ext, file)
}


private fun toDoubleVector(x: Number?, y: Number?): DoubleVector? {
    return if (x != null && y != null) {
        DoubleVector(x.toDouble(), y.toDouble())
    } else {
        null
    }
}

private fun Number.toFiniteDouble(): Double? {
    val v = this.toDouble()
    return if (v.isFinite()) v else null
}
