/*
 * Copyright (c) 2022. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.vis.svgMapper.skia.mapper


import jetbrains.datalore.vis.svg.*
import jetbrains.datalore.vis.svgMapper.skia.mapper.attr.*
import jetbrains.datalore.vis.svgMapper.skia.mapper.drawing.*
import kotlin.reflect.KClass


internal object SvgUtils {
    @Suppress("UNCHECKED_CAST")
    private val ATTR_MAPPINGS: Map<KClass<out Element>, SvgAttrMapping<Element>> = mapOf(
        Pane::class to (SvgSvgAttrMapping as SvgAttrMapping<Element>),
        //StackPane::class to (SvgSvgAttrMapping as SvgAttrMapping<Element>),
        Group::class to (SvgGAttrMapping as SvgAttrMapping<Element>),
        Rectangle::class to (SvgRectAttrMapping as SvgAttrMapping<Element>),
        Line::class to (SvgLineAttrMapping as SvgAttrMapping<Element>),
        Ellipse::class to (SvgEllipseAttrMapping as SvgAttrMapping<Element>),
        Circle::class to (SvgCircleAttrMapping as SvgAttrMapping<Element>),
        //Text::class to (SvgTextElementAttrMapping as SvgAttrMapping<Element>),
        Path::class to (SvgPathAttrMapping as SvgAttrMapping<Element>),
        Image::class to (SvgImageAttrMapping as SvgAttrMapping<Element>)
    )

    fun elementChildren(e: Element): MutableList<Element> {
        return object : AbstractMutableList<Element>() {
            override val size: Int
                get() = getChildren(e).size

            override fun get(index: Int): Element {
                return getChildren(e)[index]
            }

            override fun set(index: Int, element: Element): Element {
                if (element.parent != null) {
                    throw IllegalStateException()
                }
                return getChildren(e).set(index, element)
            }

            override fun add(index: Int, element: Element) {
                if (element.parent != null) {
                    throw IllegalStateException()
                }
                getChildren(e).add(index, element)
            }

            override fun removeAt(index: Int): Element {
                return getChildren(e).removeAt(index)
            }
        }
    }

    fun getChildren(parent: Element): MutableList<Element> {
        return when (parent) {
            is Group -> parent.children
            is Pane -> parent.children
            else -> throw IllegalArgumentException("Unsupported parent type: ${parent::class.simpleName}")
        }
    }

    fun newElement(source: SvgNode): Element {
        return when (source) {
            is SvgEllipseElement -> Ellipse()
            is SvgCircleElement -> Circle()
            is SvgRectElement -> Rectangle()
            is SvgTextElement -> Text()
            is SvgPathElement -> Path()
            is SvgLineElement -> Line()
            is SvgSvgElement -> Pane()
            is SvgGElement -> Group()
            is SvgStyleElement -> Group()
//            is SvgTextNode -> myDoc.createTextNode(null)
//            is SvgTSpanElement -> SVGOMTSpanElement(null, myDoc)
            is SvgDefsElement -> Group()
//            is SvgClipPathElement -> SVGOMClipPathElement(null, myDoc)
            is SvgImageElement -> Image()
            else -> throw IllegalArgumentException("Unsupported source svg element: ${source::class.simpleName}")
        }
    }

    fun setAttribute(target: Element, name: String, value: Any?) {
        val attrMapping = ATTR_MAPPINGS[target::class]
        attrMapping?.setAttribute(target, name, value)
        //?: throw IllegalArgumentException("Unsupported target: ${target::class}")
            ?: println("Unsupported target: ${target::class}")
    }

    fun copyAttributes(source: SvgElement, target: SvgElement) {
        for (attributeSpec in source.attributeKeys) {
            @Suppress("UNCHECKED_CAST")
            val spec = attributeSpec as SvgAttributeSpec<Any?>
            target.setAttribute(spec, source.getAttribute(attributeSpec).get())
        }
    }
}

