/*
 * Copyright (c) 2019. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.vis.svgMapper.skia.mapper.attr

import jetbrains.datalore.vis.svg.SvgCircleElement
import jetbrains.datalore.vis.svgMapper.skia.mapper.drawing.Circle

internal object SvgCircleAttrMapping : SvgShapeMapping<Circle>() {
    override fun setAttribute(target: Circle, name: String, value: Any?) {
        when (name) {
            SvgCircleElement.CX.name -> target.centerX = value?.asFloat ?: 0.0f
            SvgCircleElement.CY.name -> target.centerY = value?.asFloat ?: 0.0f
            SvgCircleElement.R.name -> target.radius = value?.asFloat ?: 0.0f
            else -> super.setAttribute(target, name, value)
        }
    }
}