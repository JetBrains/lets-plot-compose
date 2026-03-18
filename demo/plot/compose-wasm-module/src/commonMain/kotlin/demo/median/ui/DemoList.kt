/*
 * Copyright (c) 2023 JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package demo.plot.median.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Suppress("FunctionName")
@Composable
fun DemoList(
    options: List<String>,
    selectedIndex: MutableState<Int>
) {
    Box() {
        Column(
            modifier = Modifier
                .width(IntrinsicSize.Max)
        ) {
            options.forEachIndexed { index, name ->

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .selectable(
                            selected = selectedIndex.value == index,
                            onClick = { selectedIndex.value = index }
                        )
                ) {
                    RadioButton(
                        onClick = { selectedIndex.value = index },
                        selected = index == selectedIndex.value,
                    )
                    Text(name)
                }
            }
        }
    }
}
