/*
 * Copyright (c) 2022. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.vis.svgMapper.skia.mapper.attr

import jetbrains.datalore.base.geometry.DoubleRectangle
import jetbrains.datalore.vis.svg.*
import jetbrains.datalore.vis.svgMapper.skia.mapper.SvgTransformParser.parseSvgTransform
import jetbrains.datalore.vis.svgMapper.skia.mapper.drawing.Element
import jetbrains.datalore.vis.svgMapper.skia.mapper.drawing.SkPath
import org.jetbrains.skia.Matrix33
import org.jetbrains.skia.Rect

internal abstract class SvgAttrMapping<in TargetT : Element> {
    open fun setAttribute(target: TargetT, name: String, value: Any?) {
        when (name) {
            SvgGraphicsElement.VISIBILITY.name -> target.isVisible = visibilityAsBoolean(value)
            SvgGraphicsElement.OPACITY.name -> TODO() //target.opacity = asDouble(value)
            SvgGraphicsElement.CLIP_BOUNDS_JFX.name -> target.clipPath = (value as? DoubleRectangle)?.let {
                SkPath().addRect(Rect.makeLTRB(it.left.toFloat(), it.top.toFloat(), it.right.toFloat(), it.bottom.toFloat()))
            }
            SvgGraphicsElement.CLIP_PATH.name -> Unit // Not supported.
            SvgConstants.SVG_STYLE_ATTRIBUTE -> setStyle(value as? String ?: "", target)
            SvgStylableElement.CLASS.name -> setStyleClass(value as String?, target)
            SvgTransformable.TRANSFORM.name -> setTransform((value as SvgTransform).toString(), target)
            SvgElement.ID.name -> Unit // ignore it?

            else -> println("Unsupported attribute `$name` in ${target::class.simpleName}")
        }
    }

    private fun visibilityAsBoolean(value: Any?): Boolean {
        return when (value) {
            is Boolean -> value
            is SvgGraphicsElement.Visibility -> value == SvgGraphicsElement.Visibility.VISIBLE
            is String -> value == SvgGraphicsElement.Visibility.VISIBLE.toString() || asBoolean(value)
            else -> false
        }
    }

    private fun setStyle(style: String, target: TargetT) {
        style
            .split(";")
            .flatMap { it.split(":") }
            .windowed(2, 2)
            .forEach { (attr, value) -> setAttribute(target, attr, value) }
    }

    companion object {

        private fun setStyleClass(value: String?, target: Element) {
            target.styleClass = value?.split(" ")
        }

        private fun setTransform(value: String, target: Element) {
            target.transform = parseSvgTransform(value).fold(Matrix33.IDENTITY, Matrix33::makeConcat)
        }

        val Any.asFloat: Float
            get() = when (this) {
                is Number -> this.toFloat()
                is String -> this.toFloat()
                else -> error("Unsupported float value: $this")
            }

        fun asBoolean(value: Any?): Boolean {
            return (value as? String)?.toBoolean() ?: false
        }
    }
}