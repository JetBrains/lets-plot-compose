/*
 * Copyright (c) 2024 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.compose.canvas

import org.jetbrains.skia.*

class SkiaFontManager(
    private val typefaceResolver: ((org.jetbrains.letsPlot.core.canvas.Font) -> Typeface?) = { null }
) {

    fun matchFamiliesStyle(fontFamily: List<String>, fontStyle: FontStyle): Typeface {
        val fontConfig = fontFamily to fontStyle

        if (fontConfig in typefaceCache) {
            return typefaceCache.getValue(fontConfig)
        }

        var typeface = fontFamily.firstNotNullOfOrNull {
            FontMgr.default.matchFamilyStyle(it, fontStyle)
        }

        if (typeface == null || typeface.familyName == "") {
            typeface = fontFamily.firstNotNullOfOrNull {
                FontMgr.default.legacyMakeTypeface(it, fontStyle)
            }
        }

        if (typeface == null || typeface.familyName == "") {
            typeface = FontMgr.default.legacyMakeTypeface("sans-serif", fontStyle)
        }

        if (typeface == null || typeface.familyName == "") {
            println("Font not found: [${fontFamily.joinToString()}]")
            typeface = Typeface.makeEmpty()
        }

        typefaceCache[fontConfig] = typeface
        return typeface
    }

    fun font(typeface: Typeface, fontSize: Float): Font {
        return fontCache
            .getOrPut(typeface to fontSize) {
                Font(typeface, fontSize).apply {
                    isSubpixel = true
                }
            }
    }

    fun dispose() {
        typefaceCache.values.forEach(Typeface::close)
        typefaceCache.clear()
        fontCache.values.forEach(Font::close)
        fontCache.clear()
    }

    fun findFont(f: org.jetbrains.letsPlot.core.canvas.Font): Font {
        val typeface = typefaceResolver(f)
            ?: run {
                val fontStyle = FontStyle(
                    when (f.fontWeight) {
                        org.jetbrains.letsPlot.core.canvas.FontWeight.NORMAL -> FontWeight.NORMAL
                        org.jetbrains.letsPlot.core.canvas.FontWeight.BOLD -> FontWeight.BOLD
                    },
                    FontWidth.NORMAL,
                    when (f.fontStyle) {
                        org.jetbrains.letsPlot.core.canvas.FontStyle.NORMAL -> FontSlant.UPRIGHT
                        org.jetbrains.letsPlot.core.canvas.FontStyle.ITALIC -> FontSlant.ITALIC
                    }
                )

                matchFamiliesStyle(listOf(f.fontFamily), fontStyle)
            }

        val font = font(typeface, f.fontSize.toFloat())
        if (f.fontWeight == org.jetbrains.letsPlot.core.canvas.FontWeight.BOLD && !typeface.isBold) {
            font.isEmboldened = true // Synthesizes Bold
        }

        if (f.fontStyle == org.jetbrains.letsPlot.core.canvas.FontStyle.ITALIC && !typeface.isItalic) {
            font.skewX = -0.20f // Synthesizes Italic (standard skew value)
        }

        font.isSubpixel = true
        font.isLinearMetrics = true

        return font
    }

    private val typefaceCache = mutableMapOf<Pair<List<String>, FontStyle>, Typeface>()
    private val fontCache = mutableMapOf<Pair<Typeface, Float>, Font>()

    companion object {
        val DEFAULT = SkiaFontManager()
    }
}
