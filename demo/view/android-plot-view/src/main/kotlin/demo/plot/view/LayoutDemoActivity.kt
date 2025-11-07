package demo.plot.view

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import org.jetbrains.letsPlot.android.canvas.CanvasView2
import org.jetbrains.letsPlot.core.util.MonolithicCommon
import org.jetbrains.letsPlot.core.util.sizing.SizingPolicy
import org.jetbrains.letsPlot.intern.toSpec
import org.jetbrains.letsPlot.raster.view.PlotCanvasFigure2
import org.jetbrains.letsPlot.themes.flavorDarcula
import plotSpec.BarPlotSpec

class LayoutDemoActivity : Activity() {
    private companion object {
        private const val MAX_PLOT_WIDTH_DP = 390
        private const val MIN_PARENT_HEIGHT_DP = 50
        private const val MAX_PARENT_HEIGHT_DP = 390
    }

    private lateinit var plotSizeLabel: TextView
    private lateinit var sizingPolicyOptions: RadioGroup
    private lateinit var preserveAspectRatio: CheckBox
    private lateinit var plotWidthSlider: SeekBar
    private lateinit var plotHeightSlider: SeekBar

    private lateinit var containerOptionsGroup: ViewGroup
    private lateinit var fixedSizeOptionsGroup: ViewGroup

    private lateinit var widthOptions: RadioGroup
    private lateinit var heightOptions: RadioGroup
    private lateinit var parentContainer: FrameLayout
    private lateinit var parentWidthSlider: SeekBar
    private lateinit var parentHeightSlider: SeekBar


    private lateinit var demoView: CanvasView2
    private val plotFigure = PlotCanvasFigure2()
    private val plotSpec = (BarPlotSpec().basic + flavorDarcula()).toSpec()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_demo_activity)

        sizingPolicyOptions = findViewById(R.id.sizing_policy_options)
        preserveAspectRatio = findViewById(R.id.preserve_aspect_ratio)
        plotWidthSlider = findViewById(R.id.plot_width_slider)
        plotHeightSlider = findViewById(R.id.plot_height_slider)
        plotSizeLabel = findViewById(R.id.plot_size_label)
        containerOptionsGroup = findViewById(R.id.container_options_group)
        fixedSizeOptionsGroup = findViewById(R.id.fixed_size_options_group)

        widthOptions = findViewById(R.id.width_options)
        heightOptions = findViewById(R.id.height_options)
        parentContainer = findViewById(R.id.parent_container)
        parentWidthSlider = findViewById(R.id.parent_width_slider)
        parentHeightSlider = findViewById(R.id.parent_height_slider)

        demoView = CanvasView2(this).apply {
            figure = plotFigure
            setBackgroundColor(Color.GREEN)
        }

        parentContainer.addView(demoView)

        setupControls()

        findViewById<RadioButton>(R.id.width_match).isChecked = true
        findViewById<RadioButton>(R.id.height_match).isChecked = true
        findViewById<RadioButton>(R.id.sizing_policy_container).isChecked = true
        updateDemoViewLayoutParams()
        updateParentContainerSize()
        updatePlotOptions()
        updateUiEnabledState()
    }

    private fun setupControls() {
        sizingPolicyOptions.setOnCheckedChangeListener { _, checkedId ->
            updateUiEnabledState()
            updatePlotOptions()
        }

        preserveAspectRatio.setOnCheckedChangeListener { _, checkedId ->
            updatePlotOptions()
        }

        val plotSizeUpdateListener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updatePlotOptions()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }
        plotWidthSlider.setOnSeekBarChangeListener(plotSizeUpdateListener)
        plotHeightSlider.setOnSeekBarChangeListener(plotSizeUpdateListener)


        val layoutUpdateListener = RadioGroup.OnCheckedChangeListener { _, _ ->
            updateDemoViewLayoutParams()
        }
        widthOptions.setOnCheckedChangeListener(layoutUpdateListener)
        heightOptions.setOnCheckedChangeListener(layoutUpdateListener)

        val parentSizeUpdateListener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateParentContainerSize()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }
        parentWidthSlider.setOnSeekBarChangeListener(parentSizeUpdateListener)
        parentHeightSlider.setOnSeekBarChangeListener(parentSizeUpdateListener)
    }

    private fun updatePlotOptions() {
        val plotWidth = MAX_PLOT_WIDTH_DP * plotWidthSlider.progress / 100.0
        val plotHeight = MAX_PLOT_WIDTH_DP * plotHeightSlider.progress / 100.0

        plotSizeLabel.text = "Plot size: $plotWidth x $plotHeight"

        val sizingPolicy = when (sizingPolicyOptions.checkedRadioButtonId) {
            R.id.sizing_policy_fixed -> SizingPolicy.fixed(width = plotWidth, height = plotHeight)
            R.id.sizing_policy_container -> SizingPolicy.fitContainerSize(preserveAspectRatio.isChecked)
            else -> error("Unknown sizing policy option selected")
        }

        val processedSpec = MonolithicCommon.processRawSpecs(plotSpec, frontendOnly = false)
        plotFigure.update(processedSpec, sizingPolicy) { _ -> }
    }

    private fun updateDemoViewLayoutParams() {
        val width = getSelectedLayoutParam(widthOptions.checkedRadioButtonId)
        val height = getSelectedLayoutParam(heightOptions.checkedRadioButtonId)

        demoView.layoutParams = FrameLayout.LayoutParams(width, height)
    }

    private fun updateParentContainerSize() {
        val density = resources.displayMetrics.density
        val lp = parentContainer.layoutParams

        val parentOfContainer = parentContainer.parent as ViewGroup
        val parentMaxWidth = parentOfContainer.width - parentOfContainer.paddingStart - parentOfContainer.paddingEnd
        val progressWidth = parentWidthSlider.progress / 100f

        if (parentMaxWidth > 0) {
            lp.width = (parentMaxWidth * progressWidth).coerceAtLeast(100f).toInt()
        }

        val heightRangePx = (MAX_PARENT_HEIGHT_DP - MIN_PARENT_HEIGHT_DP) * density
        val minHeightPx = (MIN_PARENT_HEIGHT_DP * density)
        val progressHeight = parentHeightSlider.progress / 100f

        lp.height = (minHeightPx + (heightRangePx * progressHeight)).toInt()

        findViewById<TextView>(R.id.parent_container_size_label).text = "Parent size: ${lp.width} x ${lp.height}"

        parentContainer.layoutParams = lp
    }

    private fun getSelectedLayoutParam(checkedId: Int): Int {
        val density = resources.displayMetrics.density
        return when (checkedId) {
            R.id.width_match, R.id.height_match -> ViewGroup.LayoutParams.MATCH_PARENT
            R.id.width_wrap, R.id.height_wrap -> ViewGroup.LayoutParams.WRAP_CONTENT
            R.id.width_fixed, R.id.height_fixed -> (200 * density).toInt()
            else -> ViewGroup.LayoutParams.WRAP_CONTENT
        }
    }

    private fun updateUiEnabledState() {
        when (sizingPolicyOptions.checkedRadioButtonId) {
            R.id.sizing_policy_container -> {
                // Enable the "fitContainerSize" options
                setGroupEnabled(containerOptionsGroup, true)
                // Disable the "fixed" size options
                setGroupEnabled(fixedSizeOptionsGroup, false)
            }
            R.id.sizing_policy_fixed -> {
                // Disable the "fitContainerSize" options
                setGroupEnabled(containerOptionsGroup, false)
                // Enable the "fixed" size options
                setGroupEnabled(fixedSizeOptionsGroup, true)
            }
        }
    }

    private fun setGroupEnabled(viewGroup: ViewGroup, isEnabled: Boolean) {
        viewGroup.isEnabled = isEnabled
        // Loop through all the children of the ViewGroup
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            child.isEnabled = isEnabled
            // Set alpha to provide a visual cue for being disabled
            child.alpha = if (isEnabled) 1.0f else 0.5f
        }
    }
}