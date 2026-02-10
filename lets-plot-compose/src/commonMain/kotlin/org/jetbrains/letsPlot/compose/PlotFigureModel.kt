package org.jetbrains.letsPlot.compose

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import org.jetbrains.letsPlot.commons.registration.Disposable
import org.jetbrains.letsPlot.commons.registration.Registration
import org.jetbrains.letsPlot.core.interact.InteractionSpec
import org.jetbrains.letsPlot.core.interact.event.ToolEventDispatcher
import org.jetbrains.letsPlot.core.plot.builder.interact.tools.FigureModel
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
class PlotFigureModel() : FigureModel {
    private val toolEventCallbacks = mutableListOf<(Map<String, Any>) -> Unit>()
    private val disposableTools = mutableListOf<Disposable>()
    private var defaultInteractions: List<InteractionSpec> = emptyList()

    // Internal mutable state for spec overrides
    private val _specOverrideListState = mutableStateOf<List<Map<String, Any>>>(emptyList())

    /**
     * Exposed as a read-only State for plot rendering.
     * Plot components can observe this to trigger recomposition only when spec changes.
     */
    val specOverrideListState: State<List<Map<String, Any>>> = _specOverrideListState

    internal var toolEventDispatcher: ToolEventDispatcher? = null
        set(value) {
            val wereInteractions = if (value != null) {
                // De-activate and re-activate ongoing interactions when replacing the dispatcher.
                field?.deactivateAllSilently() ?: emptyMap()
            } else {
                // Shut down all interactions when the dispatcher is set to null
                field?.deactivateAll()
                emptyMap()
            }
            field = value
            value?.let { newDispatcher ->
                newDispatcher.initToolEventCallback { event ->
                    toolEventCallbacks.forEach { it(event) }
                }

                // Make sure that 'implicit' interactions are activated
                newDispatcher.deactivateInteractions(origin = ToolEventDispatcher.ORIGIN_FIGURE_IMPLICIT)
                newDispatcher.activateInteractions(
                    origin = ToolEventDispatcher.ORIGIN_FIGURE_IMPLICIT,
                    interactionSpecList = FIGURE_IMPLICIT_INTERACTIONS
                )

                // Set default interactions if any were configured
                defaultInteractions.let { defaultInteractionSpecs ->
                    newDispatcher.setDefaultInteractions(defaultInteractionSpecs)
                }

                // Reactivate explicit interactions in the new plot component
                ToolEventDispatcher.filterExplicitOrigins(wereInteractions)
                    .forEach { (origin, interactionSpecList) ->
                        newDispatcher.activateInteractions(origin, interactionSpecList)
                    }
            }
        }

    override fun addToolEventCallback(callback: (Map<String, Any>) -> Unit): Registration {
        toolEventCallbacks.add(callback)
        return Registration.onRemove {
            toolEventCallbacks.remove(callback)
        }
    }

    override fun activateInteractions(origin: String, interactionSpecList: List<InteractionSpec>) {
        toolEventDispatcher?.activateInteractions(origin, interactionSpecList)
    }

    override fun deactivateInteractions(origin: String) {
        toolEventDispatcher?.deactivateInteractions(origin)
    }

    override fun setDefaultInteractions(interactionSpecList: List<InteractionSpec>) {
        defaultInteractions = interactionSpecList
        toolEventDispatcher?.setDefaultInteractions(interactionSpecList)
    }

    override fun updateView(specOverride: Map<String, Any>?) {
        // Update the spec override list internally - this triggers recomposition of the plot
        _specOverrideListState.value = FigureModelHelper.updateSpecOverrideList(
            specOverrideList = _specOverrideListState.value,
            newSpecOverride = specOverride
        )
    }

    override fun addDisposible(disposable: Disposable) {
        disposableTools.add(disposable)
    }

    override fun dispose() {
        toolEventDispatcher?.deactivateAll()
        toolEventDispatcher = null
        toolEventCallbacks.clear()

        val disposables = ArrayList(disposableTools)
        disposableTools.clear()
        disposables.forEach { it.dispose() }
    }

    companion object {
        private val FIGURE_IMPLICIT_INTERACTIONS = listOf(InteractionSpec(InteractionSpec.Name.ROLLBACK_ALL_CHANGES))
    }
}