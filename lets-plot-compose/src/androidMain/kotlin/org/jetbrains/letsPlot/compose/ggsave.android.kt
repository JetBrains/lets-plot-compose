package org.jetbrains.letsPlot.compose

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.net.Uri
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
import java.io.IOException

/**
 * Defines the supported export formats for plots.
 * @param ext The file extension for this format.
 */
enum class PlotFormat(val ext: String) {
    PNG("png"),
    JPG("jpg"),
    SVG("svg"),
    HTML("html")
}

/**
 * Exports a plot to a given Uri in the specified format.
 *
 * @param plot The plot figure to export.
 * @param uri The destination content URI where the plot will be written.
 * @param format The format to save the plot in, as a [PlotFormat] enum.
 * @param context The Android context, used to access the ContentResolver.
 * @param scale Scaling factor for the plot.
 * @param dpi Dots per inch for raster formats.
 * @param w Width of the plot in the specified units.
 * @param h Height of the plot in the specified units.
 * @param unit The unit for the width and height parameters.
 */
fun ggsave(
    uri: Uri,
    plot: Figure,
    format: PlotFormat,
    scale: Number? = null,
    dpi: Number? = null,
    w: Number? = null,
    h: Number? = null,
    unit: SizeUnit? = null,
    context: Context,
) {
    val spec: MutableMap<String, Any> = plot.toSpec()
    val plotSize = toDoubleVector(w, h)

    val contentResolver = context.contentResolver
    contentResolver.openOutputStream(uri)?.use { outStream ->
        when (format) {
            PlotFormat.SVG -> {
                val svg = PlotSvgExportCommon.buildSvgImageFromRawSpecs(spec, plotSize, RGBEncoder.DEFAULT, true, unit ?: SizeUnit.PX)
                outStream.write(svg.toByteArray())
            }
            PlotFormat.HTML -> {
                val html = PlotHtmlExport.buildHtmlFromRawSpecs(spec, scriptUrl(VersionChecker.letsPlotJsVersion), true, plotSize)
                outStream.write(html.toByteArray())
            }
            PlotFormat.PNG, PlotFormat.JPG -> {
                val bmp = paintPlot(spec, plotSize, scale, unit, dpi)
                try {
                    val compressFormat = if (format == PlotFormat.PNG) CompressFormat.PNG else CompressFormat.JPEG
                    bmp.compress(compressFormat, 95, outStream)
                } finally {
                    bmp.recycle()
                }
            }
        }
    } ?: throw IOException("Failed to open output stream for URI: $uri")
}

private fun paintPlot(
    spec: MutableMap<String, Any>,
    plotSize: DoubleVector?,
    scale: Number?,
    unit: SizeUnit?,
    dpi: Number?
): Bitmap {
    val targetDPI = dpi?.toFiniteDouble()
    val (sizingPolicy, scaleFactor) = computeExportParameters(plotSize, targetDPI, unit, scale)

    val plotFigure = PlotCanvasFigure2()
    plotFigure.update(
        processedSpec = MonolithicCommon.processRawSpecs(spec, frontendOnly = false),
        sizingPolicy = sizingPolicy,
        computationMessagesHandler = {}
    )

    val androidCanvasPeer = AndroidCanvasPeer(scaleFactor)

    try {
        val reg = plotFigure.mapToCanvas(androidCanvasPeer)
        try {
            val canvas = androidCanvasPeer.createCanvas(plotFigure.size)
            plotFigure.paint(canvas.context2d)
            return canvas.takeSnapshot().platformBitmap
        } finally {
            reg.dispose()
        }
    } finally {
        androidCanvasPeer.dispose()
    }
}

private fun toDoubleVector(x: Number?, y: Number?): DoubleVector? {
    return if (x != null && y != null) DoubleVector(x.toDouble(), y.toDouble()) else null
}

private fun Number.toFiniteDouble(): Double? {
    val v = this.toDouble()
    return if (v.isFinite()) v else null
}