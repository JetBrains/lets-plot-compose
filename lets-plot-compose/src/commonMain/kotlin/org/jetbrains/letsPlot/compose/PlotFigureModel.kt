package org.jetbrains.letsPlot.compose

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import org.jetbrains.letsPlot.core.plot.builder.interact.tools.FigureModelBase
import org.jetbrains.letsPlot.core.plot.builder.interact.tools.FigureModelHelper

/**
 * FigureModel implementation for Compose that manages plot interactions and state.
 *
 * This class enables programmatic control of plot interactions (pan, zoom, etc.) and allows
 * external toolbars to control plots. The same FigureModel instance can be reused across
 * multiple plots - it automatically reconnects when the plot changes.
 *
 * ## Lifecycle
 * - If created externally and passed to PlotPanel/PlotPanelRaw via the `figureModel` parameter,
 *   the caller is responsible for disposal (or it lives until the window closes).
 * - If not provided (null), PlotPanelRaw creates an internal instance and handles disposal automatically.
 *
 * ## Usage
 * ```kotlin
 * // External control with toolbar
 * val figureModel = remember { PlotFigureModel() }
 * SandboxToolbarCmp(figureModel = figureModel)
 * PlotPanel(figure = myPlot, figureModel = figureModel, ...)
 *
 * // Set default interactions (e.g., Ctrl+Shift for pan/zoom)
 * figureModel.setDefaultInteractions(listOf(
 *     InteractionSpec(
 *         InteractionSpec.Name.WHEEL_ZOOM,
 *         keyModifiers = listOf(
 *             InteractionSpec.KeyModifier.CTRL,
 *             InteractionSpec.KeyModifier.SHIFT
 *         )
 *     ),
 *     InteractionSpec(
 *         InteractionSpec.Name.DRAG_PAN,
 *         keyModifiers = listOf(
 *             InteractionSpec.KeyModifier.CTRL,
 *             InteractionSpec.KeyModifier.SHIFT
 *         )
 *     )
 * ))
 * ```
 */
class PlotFigureModel() : FigureModelBase() {

    private val _specOverrideListState = mutableStateOf<List<Map<String, Any>>>(emptyList())

    /**
     * Exposed as a read-only State for plot rendering.
     * Plot components can observe this to trigger recomposition only when spec changes.
     */
    val specOverrideListState: State<List<Map<String, Any>>> = _specOverrideListState

    override fun updateView(specOverride: Map<String, Any>?) {
        // Update the spec override list internally - this triggers recomposition of the plot
        _specOverrideListState.value = FigureModelHelper.updateSpecOverrideList(
            specOverrideList = _specOverrideListState.value,
            newSpecOverride = specOverride
        )
    }
}