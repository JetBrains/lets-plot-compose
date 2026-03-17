/*
 * Copyright (c) 2023 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package demoData

object Raster {
    @Suppress("FunctionName")
    fun rasterData_Blue(): Map<String, List<*>> {
        val x = ArrayList<Double>()
        val y = ArrayList<Double>()
        val fill = ArrayList<Double>()
        val alpha = ArrayList<Double>()

        val width = 3
        val height = 3
        for (col in 0 until width) {
            for (row in 0 until height) {
                x.add(col.toDouble())
                y.add(row.toDouble())
                fill.add(col.toDouble())
                alpha.add(row.toDouble())
            }
        }

        @Suppress("DuplicatedCode")
        val map = HashMap<String, List<*>>()
        map["x"] = x
        map["y"] = y
        map["fill"] = fill
        map["alpha"] = alpha
        return map
    }

    @Suppress("FunctionName")
    fun rasterData_RGB(): Map<String, List<*>> {
        //  R  |  G  |  B    alpha = 1
        //  R  |  G  |  B    alpha = 0.5
        // .5  |  1  |  .5   <-- gray, alpha

        val fillInt = intArrayOf(0xFF0000, 0xFF00, 0xFF, 0xFF0000, 0xFF00, 0xFF, 0x7F0000, 0x7F00, 0x7F)

        val fill = ArrayList<Double>()
        for (i in fillInt) {
            fill.add(i.toDouble())
        }

        val alpha = listOf(1.0, 1.0, 1.0, 0.5, 0.5, 0.5, 0.5, 1.0, 0.5)
        val x = listOf(0.0, 1.0, 2.0, 0.0, 1.0, 2.0, 0.0, 1.0, 2.0)
        val y = listOf(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, 2.0, 2.0, 2.0)

        val map = HashMap<String, List<*>>()
        map["x"] = x
        map["y"] = y
        map["fill"] = fill
        map["alpha"] = alpha
        return map
    }
}