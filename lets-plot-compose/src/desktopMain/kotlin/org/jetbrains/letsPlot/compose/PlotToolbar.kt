/*
 * Copyright (c) 2024 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import org.jetbrains.letsPlot.commons.registration.Registration
import org.jetbrains.letsPlot.core.plot.builder.interact.tools.DefaultFigureToolsController
import org.jetbrains.letsPlot.core.plot.builder.interact.tools.ToggleTool
import org.jetbrains.letsPlot.core.plot.builder.interact.tools.ToggleToolModel
import org.jetbrains.letsPlot.core.plot.builder.interact.tools.ToolSpecs.BBOX_ZOOM_TOOL_SPEC
import org.jetbrains.letsPlot.core.plot.builder.interact.tools.ToolSpecs.CBOX_ZOOM_TOOL_SPEC
import org.jetbrains.letsPlot.core.plot.builder.interact.tools.ToolSpecs.PAN_TOOL_SPEC
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.util.*
import javax.imageio.ImageIO


@Suppress("FunctionName")
@Composable
fun PlotToolbar(figureModel: PlotFigureModel) {
    var registration by remember { mutableStateOf(Registration.EMPTY) }
    var panToolState by remember { mutableStateOf(false) }
    var bboxZoomToolState by remember { mutableStateOf(false) }
    var cboxZoomToolState by remember { mutableStateOf(false) }

    val panTool = remember { ToggleTool(PAN_TOOL_SPEC) }
    val bboxZoomTool = remember { ToggleTool(BBOX_ZOOM_TOOL_SPEC) }
    val cboxZoomTool = remember { ToggleTool(CBOX_ZOOM_TOOL_SPEC) }

    val controller = remember {
        DefaultFigureToolsController(figureModel, errorMessageHandler = { println(it) }).also {
            registration = figureModel.addToolEventCallback { event ->
                it.handleToolFeedback(event)
            }
        }
    }

    val panToolModel = remember {
        object : ToggleToolModel() {
            override fun setState(selected: Boolean) {
                panToolState = selected
            }
        }.also { controller.registerTool(panTool, it) }
    }

    val bboxZoomToolModel = remember {
        object : ToggleToolModel() {
            override fun setState(selected: Boolean) {
                bboxZoomToolState = selected
            }
        }.also { controller.registerTool(bboxZoomTool, it) }
    }

    val cboxZoomToolModel = remember {
        object : ToggleToolModel() {
            override fun setState(selected: Boolean) {
                cboxZoomToolState = selected
            }
        }.also { controller.registerTool(cboxZoomTool, it) }
    }

    DisposableEffect(figureModel) {
        onDispose {
            registration.remove()
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        IconButton(
            icon = if (panToolState) PAN_ACTIVE_ICON else PAN_ICON,
            modifier = Modifier.padding(3.dp).size(24.dp),
            onClick = { panToolModel.action() }
        )

        IconButton(
            icon = if (bboxZoomToolState) BBOX_ZOOM_ACTIVE_ICON else BBOX_ZOOM_ICON,
            modifier = Modifier.padding(3.dp).size(24.dp),
            onClick = { bboxZoomToolModel.action() }
        )

        IconButton(
            icon = if (cboxZoomToolState) CBOX_ZOOM_ACTIVE_ICON else CBOX_ZOOM_ICON,
            modifier = Modifier.padding(3.dp).size(24.dp),
            onClick = { cboxZoomToolModel.action() }
        )

        IconButton(
            icon = RESET_ICON,
            modifier = Modifier.padding(3.dp).size(24.dp),
            onClick = { controller.resetFigure(deactiveTools = true) }
        )
    }
}


private fun base64ToImageBitmap(base64: String): ImageBitmap {
    val base64Data = base64.substringAfter("base64,")
    val decodedBytes = Base64.getDecoder().decode(base64Data)
    val inputStream = ByteArrayInputStream(decodedBytes)
    val bufferedImage: BufferedImage = ImageIO.read(inputStream)
    return bufferedImage.toComposeImageBitmap()
}

@Composable
private fun IconButton(icon: ImageBitmap, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .then(modifier)
    ) {
        Image(
            bitmap = icon,
            contentDescription = null,
            modifier = Modifier.size(icon.width.dp, icon.height.dp)
        )
    }
}

private val PAN_ICON =
    base64ToImageBitmap("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAACXBIWXMAAB2HAAAdhwGP5fFlAAAAGXRFWHRTb2Z0d2FyZQB3d3cuaW5rc2NhcGUub3Jnm+48GgAAAPNJREFUWIXtlVEOgyAQRGd6h5J6RDxu/ehxph/FhiiILKbUxE32B2XmuS4LJaFn3Lq6twKQFMmmEp67AhfATwBIepKuVpikI+mLL0rKJoARgABMAFziuT4Sq3UX9gjAuOlRALgDeAahF4ChBJDY8zADlCCWALXmuwC2IGIAi/lugBzEDGA1rwJINNcUAcRrq2Y9DCBRiTirvnxOBlG0Xiq1IYnAH0zCo3/BUK13miZE4qghfwx3V8JsHta3BtEuCLP5EsAKYTZPAVggSgB9r+Mg5nOdnQOIIHxJ/zsJLTFPT4WpZonuk/AC6A7Q1IRHRPcKvAGpnEGs1cIJFQAAAABJRU5ErkJggg==")
private val PAN_ACTIVE_ICON =
    base64ToImageBitmap("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAACXBIWXMAAB2HAAAdhwGP5fFlAAAAGXRFWHRTb2Z0d2FyZQB3d3cuaW5rc2NhcGUub3Jnm+48GgAAAi5JREFUWIXNl0FPE0EcxX8zu4lgirQeBDEmeJKLypHQ6IEoeunFLyGNMSbW1n4CUyBNuGj7Gbx4aCKRhIum6lVunGiC2koi25YGMdnd8bCyKUa3O0tKeafNzn/nvZl582ZHKKW4+7Q+hZIFF+YEjNBHKNgD1k3Hya+uTGyK+cy3KaXkRyDeT+J/oGk4zoxEyUJU8rXiGGvFsagC4o6Uz6ULc1F7ODaEuC37veY9cE4OkByA0y8gNTtMPKavMx6TpGaHe9aZgeTJszy6P0IqaZMrWTQ7bmjypXSCyXEThKBS3f9vbeDQ3n0+YKtuMzluUnyY4HwIy4zGJIsLHvn2jk114yCwPrDHVsclV7bYqttcvmCynA4WMRqTLC0kuHLRI8++tNjdC561nkMKKyIKeSgBYUREJQcQd540VKhKjpqr1vC8AfjPtYaeWUEzB5odl2zJ8o15iEPD5ct65NA1A8c4VCJhPvMdOAVJqOUBOGq4bmzv2GRLFrttvSXQmoF4TLKc9shrDdt/X2t4u6PwIKEd26Gr/064fNny27qNGTYxtQT02ue6iaklIGzIRBURWKGbcFFEBLbeujHkGy7zIly8tjouz8qWb8zktaHA+sD/gUp1H5Ti/cYvrYRrdlxyJYub189Q+fAzugCgZwdBIsJ8O/AkHLyAP3e1QaElgfWB0QveinuPv151DOMTJ305FfwwDXtarq5MbBqOMwO8BtonQN1G8Mo07Ok3i5e+/AaZ1ys9OkxDiwAAAABJRU5ErkJggg==")

private val BBOX_ZOOM_ICON =
    base64ToImageBitmap("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAACXBIWXMAAB2HAAAdhwGP5fFlAAAAGXRFWHRTb2Z0d2FyZQB3d3cuaW5rc2NhcGUub3Jnm+48GgAAANtJREFUWIXtlk0KwjAQRt8n3qJdqisP0Gv1NNobWMSVy/Yi3kHBdutiXJhKLAUX9g/MwBCShrxH0oSRmTFlLCalNwKSEkkHSaWkVNJyTIkEeADmZWZmjJEAeQs+VNbACVi3BYqRBJq8AZEvkHZMugDqeasj4OzWz32BJZC14NuBzjt2jHszpsZCkgGYmb78tD9FmzOPdyAIBIEgEASCQBAIAi5qAEnxUDBv7apLoHTtTlI0EHzvusX7g1cubYAr4xSlqze3o3DMeW1R3+AKOPrwj5pwqpjVLfhPgSdBFZH0qgJ4WwAAAABJRU5ErkJggg==")
private val BBOX_ZOOM_ACTIVE_ICON =
    base64ToImageBitmap("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAACXBIWXMAAB2HAAAdhwGP5fFlAAAAGXRFWHRTb2Z0d2FyZQB3d3cuaW5rc2NhcGUub3Jnm+48GgAAAe1JREFUWIXtl89LFGEYxz/vO7OOKesushEYVATFKhQqgR6iIKxuHcL+gToURCxkB08dgmI9CKt3JQg6VBBCKh2EIqEfkEWHQrt02EzQDXenZmdl33k7LDvrggTBTF7me5p5GObzeXnew/MIrTXnb/1Io2XWgzMC4oQYDTawYCo1Op/rWhbnRlbTWsvXQDJM8A7ZNJQaNNEy230wlrx4qo2OdsnbzxVmFh2UF7pAUkl5z+w+FDs7fr0TQ9aqfUdaOLDPJPe4FLoBQgyJFx/K+nRva+isckXzfmWLqWc23zeUX5fxNhk6HGCPJTh5zGIy00kq0WCa775U6D/a0vRxfr3KlbECWgcnkEpIMsMdDPRYXL0Q5+6DIgByZtFh7k25CX7nfjFQOMBG0WPiiQ3AibTl16XyaLpwl7MFvq1Vg6X7ErXet7eKhkAopH9IJBAJRAKRQCQQCUQCvoDj1iaQVMIIDVb/92+3Me34AktftwDIDMebZrYg4ZlLtZ1naaXi1836w/SszfHDMQZ6LB7e3hu4QD224zE1+8t/94+aX1dcGy/w8qPrtyPIOK7m1SeXGxM/Wd02louhm2ulsPfBv6QogYVdgoPguTSVGgU2dwFeMI3qiJzPdS0bSg0CT4H/sBBSQvDINKq9c2P7838AoiGjAsFi+oIAAAAASUVORK5CYII=")

private val CBOX_ZOOM_ICON =
    base64ToImageBitmap("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAACXBIWXMAAB2HAAAdhwGP5fFlAAAAGXRFWHRTb2Z0d2FyZQB3d3cuaW5rc2NhcGUub3Jnm+48GgAAARFJREFUWIXtlrFuwjAQhr+/Q14ijMCzpk/Q7p1QxMQIrwNdilSyV9chTmSCJaJTQqiak06RHcf/J/vucjIzprSXSdVnAAAzax1YACVQATawV8AWWF1pdsTPIwh3/QzkKYAyLNjFC4ZyIA97G7BJATTHPrh455QN+G7m1NQBSRZiQo5Q6m1dncmzYAb4mwCSMkmvkk6SjpIKSZmLIEoRq4e90qngtsAUPb+90vECnBIAnx4Abwykmogfz0ZegI+ec/fNeQUZdRwcgxdA5rmCuRTPAE8FUAFIWowlFu19SQEcwvNNUj6S+HsY7tsXUX6ugS8e05Qub/4FUeO4oT6ioYUv1I3vMtZsC9FU9lRZ8D8BfgFquENnSQFiqAAAAABJRU5ErkJggg==")
private val CBOX_ZOOM_ACTIVE_ICON =
    base64ToImageBitmap("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAACXBIWXMAAB2HAAAdhwGP5fFlAAAAGXRFWHRTb2Z0d2FyZQB3d3cuaW5rc2NhcGUub3Jnm+48GgAAAjRJREFUWIXtlz9oE1Ecxz/37sqZpmmKxKVuOjQU/ANG2sHFEP8t0qHqqFvtIAXboZObUkGhGR0iVMFBCi7GVkgHcRAhLeIgtK61CibSJM2/kpfncM2lmqA23CUO/U53X9697/e97+/3eKcppbgw9TWIEjNVCGvgw0UoyAFLhpTTC7P9q9r5yY2gUuId0OemcBNs6lIOC5SY6YA4QJ8U4p6oQrgD4hY0LWLszjzg1xkf6SE0YOIxNUe1imXF8to2sZc5vqRkje41dos/mjqIr1s4KlyDx9Q4c8zkxNEuxh6kSWWqANgGxkd68HUL3n8qE53P2gOcQsAvmBjtZWjQZOyyj7tPMwDYyw0NmACuiAOkMlWi8zlLK2javG2glrkb4nUTVvbeA/X6cifwPWDfQMcNGH8f0uQjXePGJS+RUx4UkEgWmVvMU5GqPQauX/Ry9azXfr8Wtp5j8a09z9VSBOdCnkbudCPnmoFmG11t8fhoyUAiWWzklhu5f0FLNTC3mAcgshNFIlnkyQ7XFgMVqYjFt1oqut/R8XNg38D/Y6BQsro74NddE6vNnS/VTxLbwMrnbQAmRn0E/M5vTMCvM3HFuv+urJVt3m7Dx/Ecx490MTRo8uzOIccN1JArVH9pX3up698lNx+mefOhZMfhJAolxduPJW5Ff7BRv5ajRW5/y7r9P/gHZASw1CFx0HgtDCmngc0OiKcNvTIpFmb7V3Uph4EXQLYN0lk0nht65eSr+4fXfwL+SbOhiKONmwAAAABJRU5ErkJggg==")

private val RESET_ICON =
    base64ToImageBitmap("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAACXBIWXMAAB2HAAAdhwGP5fFlAAAAGXRFWHRTb2Z0d2FyZQB3d3cuaW5rc2NhcGUub3Jnm+48GgAAAj5JREFUWIXN17trVEEUx/HPJPERo4kRjREhELSJ2EtQBEFTJSD4akT/AAtr/Qv8CwSxsBKtBAuxND5ADL4IRgyCxEIxKhGMr8qxuLN6s+xu7m6yGweGs3cev/O9h7nnzIYYo5VsbSvqvRJACCGGEBYNSwihLYTQG0LoWgpAR9GFIYR2jGIMezGINWluFtO4gxsxxsnCBDHGBR0xG/77HHASM6W51H9iDl/LxiPuYn+5dqVeEwC9uJ0TfoYz2IX23J4tGMFFfM6tv4TOhgDQj1fp9wccL/RGrMd5fM9Bb24E4Gmyk9haxHmZzlDaW9LaWC9AxFQt+gIQm1IEIq43AlCx1wnRj09p79GWAyTNE2nvC4SaAM3o6VMuHeiD+bmWpOKYUVxOj2P5uVbWggfJDucHQwpR01sIoRM/MB9j7C6NtzICv5JdnR9cABBCmE/VcEkVrkprw29lBbA8Ah+T7WsCwM7kb6YWwPtkdzQBYHeyU7UAxpMdaQJA6fObWDBaljCGZcnitVy5XYZE1IV52RkYrJoJU0RKFez0MgKcS5rjRWrB4bT4nQbKcAW9AXxLb7+vCEDAzQRxD+uW4LwHz5PWlYpramycThsnsK0B59vxMGk8QXdhgJxAif4LzmJtAccdOCXLKREv0Vd1/SJi3bjq311gTnbxPCa7lvfKUusADuEC3uTWX8OGmj4KhvMA7qtyQanQH2G0iHZd1TCEMIQj2CPLbD1YhVm8TYf2VozxcWHNegCa0f6/P6etbn8Amy2DASz9UqwAAAAASUVORK5CYII=")

