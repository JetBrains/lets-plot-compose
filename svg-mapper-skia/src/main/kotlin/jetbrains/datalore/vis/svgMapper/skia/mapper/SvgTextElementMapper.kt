/*
 * Copyright (c) 2022. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.vis.svgMapper.skia.mapper

import jetbrains.datalore.base.observable.collections.ObservableCollection
import jetbrains.datalore.base.observable.property.ReadableProperty
import jetbrains.datalore.base.observable.property.SimpleCollectionProperty
import jetbrains.datalore.base.observable.property.WritableProperty
import jetbrains.datalore.mapper.core.Synchronizers
import jetbrains.datalore.vis.StyleSheet
import jetbrains.datalore.vis.svg.*
import jetbrains.datalore.vis.svgMapper.skia.asSkiaColor
import jetbrains.datalore.vis.svgMapper.skia.mapper.attr.SvgTextElementAttrMapping
import jetbrains.datalore.vis.svgMapper.skia.mapper.drawing.Text
import org.jetbrains.skia.FontStyle

internal class SvgTextElementMapper(
    source: SvgTextElement,
    target: Text,
    peer: SvgSkiaPeer
) : SvgElementMapper<SvgTextElement, Text>(source, target, peer) {

    private val myTextAttrSupport = TextAttributesSupport(target)

    override fun setTargetAttribute(name: String, value: Any?) {
//        println("text -> $name = $value")
//        println("text -> ${target.font}")
//        val def = Font.getDefault()
//        println("text -> ${def.family}")
//        val font = Font.font(def.family, FontPosture.ITALIC, def.size)
//        val font = Font.font("Times New Roman", FontPosture.ITALIC, def.size)
//        target.font = font
//        println("text -> ${target.font}")
//        Font.getFamilies().forEach { println(it) }
//        Font.getFontNames().forEach { println(it) }
//        throw RuntimeException("The End")


        myTextAttrSupport.setAttribute(name, value)
    }

    override fun applyStyle() {
        setFontProperties(target, peer.styleSheet)
    }

    override fun registerSynchronizers(conf: SynchronizersConfiguration) {
        super.registerSynchronizers(conf)

        // Sync TextNodes, TextSpans
        val sourceTextProperty = sourceTextProperty(source.children())
//        sourceTextProperty.addHandler(object : EventHandler<PropertyChangeEvent<out String>> {
//            override fun onEvent(event: PropertyChangeEvent<out String>) {
//                println("new text: ${event.newValue}")
//            }
//        })
        conf.add(
            Synchronizers.forPropsOneWay(
                sourceTextProperty,
                targetTextProperty(target)
            )
        )
    }

    private fun setFontProperties(target: Text, styleSheet: StyleSheet?) {
        if (styleSheet == null) {
            return
        }
        val className = source.fullClass()
        if (className.isNotEmpty()) {
            val style = styleSheet.getTextStyle(className)
            target.fill = style.color.asSkiaColor
            target.fontFamily = style.family.split(",").map { it.trim(' ', '"') }
            target.fontSize = style.size.toFloat()
            target.fontStyle = when  {
                style.face.bold && !style.face.italic -> FontStyle.BOLD
                style.face.bold && style.face.italic -> FontStyle.BOLD_ITALIC
                !style.face.bold && style.face.italic -> FontStyle.ITALIC
                !style.face.bold && !style.face.italic -> FontStyle.NORMAL
                else -> error("Unknown fontStyle: `${ style.face.toString() }`")
            }

            myTextAttrSupport.setAttribute(SvgConstants.SVG_STYLE_ATTRIBUTE, "fill:${style.color.toHexColor()};")
        }
    }

    companion object {
        private fun sourceTextProperty(nodes: ObservableCollection<SvgNode>): ReadableProperty<String> {
            return object : SimpleCollectionProperty<SvgNode, String>(nodes, joinToString(nodes)) {
                override val propExpr = "joinToString($collection)"
                override fun doGet() = joinToString(collection)
            }
        }

        private fun joinToString(nodes: ObservableCollection<SvgNode>): String {
            return nodes.asSequence()
                .flatMap{((it as? SvgTSpanElement)?.children() ?: listOf(it as SvgTextNode)).asSequence()}
                .joinToString ("\n") { (it as SvgTextNode).textContent().get() }
        }

        private fun targetTextProperty(target: Text): WritableProperty<String?> {
            return object : WritableProperty<String?> {
                override fun set(value: String?) {
                    target.text = value ?: "n/a"
                }
            }
        }
    }

    private class TextAttributesSupport(val target: Text) {
        private var mySvgTextAnchor: String? = null

        fun setAttribute(name: String, value: Any?) {
            if (name == SvgTextContent.TEXT_ANCHOR.name) {
                mySvgTextAnchor = value as String?
            }
            SvgTextElementAttrMapping.setAttribute(target, name, value)
        }
    }
}