package org.jetbrains.letsPlot.android

import android.graphics.Typeface
import androidx.test.platform.app.InstrumentationRegistry
import org.jetbrains.letsPlot.android.canvas.AndroidFontManager
import org.jetbrains.letsPlot.core.canvas.Font.FontVariant.*
import java.io.IOException


object NotoFontManager {
    private val notoSans = mapOf(
        NORMAL to createFont("font/NotoSans-Regular.ttf"),
        BOLD to createFont("font/NotoSans-Bold.ttf"),
        ITALIC to createFont("font/NotoSans-Italic.ttf"),
        BOLD_ITALIC to createFont("font/NotoSans-BoldItalic.ttf")
    )

    private val notoSerif = mapOf(
        NORMAL to createFont("font/NotoSerif-Regular.ttf"),
        BOLD to createFont("font/NotoSerif-Bold.ttf"),
        ITALIC to createFont("font/NotoSerif-Italic.ttf"),
        BOLD_ITALIC to createFont("font/NotoSerif-BoldItalic.ttf")
    )

    private val notoMono = mapOf(
        NORMAL to createFont("font/NotoSansMono-Regular.ttf"),
        BOLD to createFont("font/NotoSansMono-Bold.ttf"),
        ITALIC to createFont("font/NotoSansMono-Regular.ttf", style = Typeface.ITALIC),
        BOLD_ITALIC to createFont("font/NotoSansMono-Bold.ttf", style = Typeface.ITALIC)
    )

    private fun createFont(resourceName: String, style: Int? = null): Typeface? {
        try {
            val context = InstrumentationRegistry.getInstrumentation().context
            val typeface = Typeface.createFromAsset(context.assets, resourceName)
            if (typeface == null) {
                println("Failed to load font: $resourceName")
                throw IOException("Failed to load font: $resourceName")
            }

            if (style != null) {
                return Typeface.create(typeface, style)
            } else {
                return typeface
            }
        } finally {
            try {
            } catch (e: IOException) {
                e.printStackTrace()
                throw RuntimeException("Failed to load font: $resourceName", e)
            }
        }
    }


    val INSTANCE = AndroidFontManager(
        typefaceResolver = { font ->
            when (font.fontFamily) {
                "Noto Sans Regular" -> notoSans[NORMAL]
                "Noto Serif Regular" -> notoSerif[NORMAL]
                "Noto Sans Mono Regular" -> notoMono[NORMAL]

                else -> {
                    val fontFamily = when {
                        "Noto Sans" == font.fontFamily -> notoSans
                        "Noto Serif" == font.fontFamily -> notoSerif
                        "Noto Sans Mono" == font.fontFamily -> notoMono
                        "mono" in font.fontFamily -> notoMono
                        "sans-serif" in font.fontFamily -> notoSans
                        "sans" in font.fontFamily -> notoSans
                        "serif" in font.fontFamily -> notoSerif
                        else -> notoSans // default font family
                    }

                    fontFamily[font.variant]
                }
            }
        }
    )
}
