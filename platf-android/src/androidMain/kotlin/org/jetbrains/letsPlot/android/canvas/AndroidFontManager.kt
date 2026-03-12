/*
 * Copyright (c) 2026 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.android.canvas

import android.graphics.Typeface


class AndroidFontManager(
    private val typefaceResolver: ((org.jetbrains.letsPlot.core.canvas.Font) -> Typeface?) = { null }
) {
    fun getTypeface(f: org.jetbrains.letsPlot.core.canvas.Font): Typeface? {
        val typeface = typefaceResolver(f)
        if (typeface != null) {
            return typeface
        }

        val style = when(f.variant) {
            org.jetbrains.letsPlot.core.canvas.Font.FontVariant.BOLD_ITALIC -> Typeface.BOLD_ITALIC
            org.jetbrains.letsPlot.core.canvas.Font.FontVariant.BOLD -> Typeface.BOLD
            org.jetbrains.letsPlot.core.canvas.Font.FontVariant.ITALIC -> Typeface.ITALIC
            org.jetbrains.letsPlot.core.canvas.Font.FontVariant.NORMAL -> Typeface.NORMAL
        }


        return Typeface.create(f.fontFamily, style)
    }

    companion object {
        val DEFAULT = AndroidFontManager()
    }
}
