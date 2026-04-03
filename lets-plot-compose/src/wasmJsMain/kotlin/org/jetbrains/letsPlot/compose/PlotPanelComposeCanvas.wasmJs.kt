/*
 * Copyright (c) 2026. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import org.jetbrains.letsPlot.commons.geometry.DoubleVector
import org.jetbrains.letsPlot.commons.registration.CompositeRegistration
import org.jetbrains.letsPlot.commons.registration.Registration
import org.jetbrains.letsPlot.compose.canvas.SkiaCanvasPeer
import org.jetbrains.letsPlot.compose.canvas.SkiaContext2d
import org.jetbrains.letsPlot.compose.canvas.SkiaFontManager
import org.jetbrains.letsPlot.raster.view.PlotCanvasDrawable

@Composable
internal actual fun PlatformPlotToolbar(
    processedPlotSpec: MutableMap<String, Any>,
    figureModel: PlotFigureModel
) {
}

internal actual fun Modifier.plotPointerHoverIcon(): Modifier = this

internal actual fun createPlatformPlotComponentRegistration(
    plotDrawable: PlotCanvasDrawable,
    onRepaintRequested: () -> Unit
): Registration {
    plotDrawable.onHrefClick(::browseLink)
    return CompositeRegistration(
        plotDrawable.onRepaintRequested(onRepaintRequested),
        plotDrawable.mapToCanvas(SkiaCanvasPeer(SkiaFontManager.DEFAULT)),
        Registration.onRemove {
            plotDrawable.onHrefClick(handler = {})
        }
    )
}

internal actual fun DrawScope.paintPlatformPlot(
    plotDrawable: PlotCanvasDrawable,
    density: Float,
    plotPosition: DoubleVector
) {
    val ctx = SkiaContext2d(drawContext.canvas.nativeCanvas, SkiaFontManager.DEFAULT)
    ctx.scale(density.toDouble(), density.toDouble())
    ctx.translate(plotPosition.x, plotPosition.y)
    plotDrawable.paint(ctx)
    ctx.dispose()
}

private fun browseLink(string: String) {
}
