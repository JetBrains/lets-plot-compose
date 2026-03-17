/*
 * Copyright (c) 2023 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package plotSpec

interface PlotDemoSpec {
    fun createRawSpec(): MutableMap<String, Any> {
        return createRawSpecList().first()
    }

    fun createRawSpecList(): List<MutableMap<String, Any>>
}