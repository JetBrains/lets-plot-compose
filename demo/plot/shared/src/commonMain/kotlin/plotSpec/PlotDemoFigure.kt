/*
 * Copyright (c) 2023 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package plotSpec

import org.jetbrains.letsPlot.Figure

interface PlotDemoFigure {
    fun createFigure(): Figure {
        return createFigureList().first()
    }

    fun createFigureList(): List<Figure>
}