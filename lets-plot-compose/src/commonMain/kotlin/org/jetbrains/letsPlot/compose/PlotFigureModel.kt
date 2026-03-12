package org.jetbrains.letsPlot.compose

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import org.jetbrains.letsPlot.core.plot.builder.interact.tools.FigureModelBase
import org.jetbrains.letsPlot.core.plot.builder.interact.tools.FigureModelHelper
import org.jetbrains.letsPlot.core.plot.builder.interact.tools.FigureModelOptions.TARGET_ID
import org.jetbrains.letsPlot.core.plot.builder.interact.tools.SpecOverrideState

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

    private var currSpecOverrideList: List<Map<String, Any>> = emptyList()

    private val _specOverrideState = mutableStateOf(SpecOverrideState.empty())

    /**
     * Exposed as a read-only State for plot rendering.
     * Plot components observe this to trigger recomposition when spec overrides change.
     */
    val specOverrideState: State<SpecOverrideState> = _specOverrideState

    override fun updateView(specOverride: Map<String, Any>?) {
        // Sync with any expansion that happened during the previous rebuild.
        val currentState = _specOverrideState.value
        if (currentState.expandedOverrides.isNotEmpty()) {
            currSpecOverrideList = currentState.expandedOverrides
        }

        currSpecOverrideList = FigureModelHelper.updateSpecOverrideList(
            specOverrideList = currSpecOverrideList,
            newSpecOverride = specOverride
        )

        val activeTargetId = specOverride?.get(TARGET_ID) as? String

        // Create a new state instance to trigger recomposition.
        _specOverrideState.value = SpecOverrideState(currSpecOverrideList, activeTargetId)
    }
}
