/*
 * Copyright (c) 2026. JetBrains s.r.o.
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

    fun font(typeface: Typeface, fontSize: Float, emboldened: Boolean = false, skewX: Float = 0f): Font {
        return fontCache.getOrPut(FontKey(typeface, fontSize, emboldened, skewX)) {
            Font(typeface, fontSize).apply {
                isSubpixel = true
                isLinearMetrics = true
                isEmboldened = emboldened
                this.skewX = skewX
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

        // Synthesize Bold/Italic only when the resolved typeface lacks the real variant (common on
        // Wasm, where the font set is limited). Bake the synthetic flags into the cache key so each
        // (typeface, size, weight, style) gets its own immutable Font - never mutate a shared
        // instance, otherwise measured and drawn widths diverge and text overflows its layout box.
        val emboldened = f.fontWeight == org.jetbrains.letsPlot.core.canvas.FontWeight.BOLD && !typeface.isBold
        val skewX = if (f.fontStyle == org.jetbrains.letsPlot.core.canvas.FontStyle.ITALIC && !typeface.isItalic) {
            -0.20f // Synthesizes Italic (standard skew value)
        } else {
            0f
        }

        return font(typeface, f.fontSize.toFloat(), emboldened, skewX)
    }

    private val typefaceCache = mutableMapOf<Pair<List<String>, FontStyle>, Typeface>()
    private val fontCache = mutableMapOf<FontKey, Font>()

    private data class FontKey(
        val typeface: Typeface,
        val fontSize: Float,
        val emboldened: Boolean,
        val skewX: Float
    )

    companion object {
        val DEFAULT = SkiaFontManager()
    }
}
