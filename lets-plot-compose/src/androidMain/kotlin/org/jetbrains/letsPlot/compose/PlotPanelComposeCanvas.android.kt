/*
 * Copyright (c) 2026. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.compose

import android.view.PointerIcon.TYPE_CROSSHAIR
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import org.jetbrains.letsPlot.android.canvas.AndroidCanvasPeer
import org.jetbrains.letsPlot.android.canvas.AndroidContext2d
import org.jetbrains.letsPlot.android.canvas.AndroidFontManager
import org.jetbrains.letsPlot.commons.geometry.DoubleVector
import org.jetbrains.letsPlot.commons.registration.Registration
import org.jetbrains.letsPlot.raster.view.PlotCanvasDrawable

@Composable
internal actual fun PlatformPlotToolbar(
    processedPlotSpec: MutableMap<String, Any>,
    figureModel: PlotFigureModel
) {
}

internal actual fun Modifier.plotPointerHoverIcon(): Modifier {
    return pointerHoverIcon(PointerIcon(TYPE_CROSSHAIR))
}

internal actual fun createPlatformPlotComponentRegistration(
    plotDrawable: PlotCanvasDrawable,
    onRepaintRequested: () -> Unit
): Registration {
    return Registration.from(
        plotDrawable.onRepaintRequested(onRepaintRequested),
        plotDrawable.mapToCanvas(AndroidCanvasPeer())
    )
}

internal actual fun DrawScope.paintPlatformPlot(
    plotDrawable: PlotCanvasDrawable,
    density: Float,
    plotPosition: DoubleVector
) {
    val ctx = AndroidContext2d(drawContext.canvas.nativeCanvas, AndroidFontManager.DEFAULT)
    ctx.scale(density.toDouble(), density.toDouble())
    ctx.translate(plotPosition.x, plotPosition.y)
    plotDrawable.paint(ctx)
}
