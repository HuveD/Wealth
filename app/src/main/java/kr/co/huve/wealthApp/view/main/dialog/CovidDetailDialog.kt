package kr.co.huve.wealthApp.view.main.dialog

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.DialogFragment
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.MPPointF
import dagger.hilt.android.AndroidEntryPoint
import kr.co.huve.wealthApp.R
import kr.co.huve.wealthApp.model.repository.data.CovidItem
import kr.co.huve.wealthApp.util.isNotNull
import kr.co.huve.wealthApp.util.isNull
import kr.co.huve.wealthApp.util.notNull
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.floor

private const val DEFAULT_TEXT_SIZE = 13f

@AndroidEntryPoint
class CovidDetailDialog :
    DialogFragment() {

    private val axisHash = HashMap<Float, Long>()
    private var covidItem: List<CovidItem>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val layout = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_covid_detail, null)
            .apply {
                if (covidItem.isNull()) {
                    dismiss()
                } else {
                    val tabContainer: ViewGroup = this.findViewById(R.id.tabContainer)
                    val chart: PieChart = this.findViewById(R.id.pieChart)
                    val covidData = covidItem!!
                    initializePieChart(chart, covidData)
                    this.findViewById<ImageView>(R.id.closeButton).setOnClickListener {
                        dismiss()
                    }
                }
            }
        dialog?.window?.attributes?.apply {
            gravity = Gravity.FILL_HORIZONTAL
        }
        return layout
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    fun bind(data: List<CovidItem>?) {
        this.covidItem = data
    }

    private fun initializePieChart(
        pieChart: PieChart,
        covidItem: List<CovidItem>
    ) {
        if (axisHash.size > 0) axisHash.clear()
        pieChart.apply {
            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
                textSize = DEFAULT_TEXT_SIZE
                xEntrySpace = 8f
                textColor = ContextCompat.getColor(context, R.color.iconic_dark)
            }

            setUsePercentValues(true)
            description.isEnabled = false
            setExtraOffsets(5f, 10f, 5f, 5f)

            dragDecelerationFrictionCoef = 0.95f
            isDrawHoleEnabled = true
            setEntryLabelColor(ContextCompat.getColor(requireContext(), R.color.iconic_white))
            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(110)
            setHoleColor(Color.WHITE)

            holeRadius = 58f
            transparentCircleRadius = 61f

            centerText = covidItem.first().region
            setCenterTextTypeface(Typeface.DEFAULT_BOLD)
            setDrawCenterText(true)

            rotationAngle = 0f
            isRotationEnabled = false
            isHighlightPerTapEnabled = true

            // entry label styling
            setEntryLabelColor(ContextCompat.getColor(requireContext(), R.color.iconic_dark))
            setEntryLabelTextSize(DEFAULT_TEXT_SIZE)
            setDrawEntryLabels(false)
            highlightValues(emptyArray())
        }
        invalidateChart(pieChart, PieType.Covid, covidItem)
    }

    private fun invalidateChart(chart: PieChart, type: PieType, data: List<CovidItem>) {
        chart.apply {
            this.data = PieData(
                when (type) {
                    PieType.Covid -> applyCovidDetail(data)
                    else -> applyCovidDetail(data)
                }
            )
            this.data.setValueTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.iconic_dark
                )
            )
            this.data.setValueFormatter(PercentFormatter(this))
            this.data.setValueTextSize(DEFAULT_TEXT_SIZE)
            this.data.setValueTypeface(Typeface.DEFAULT_BOLD)
            invalidate()
        }
    }

    private fun changeStyle(container: ViewGroup, selectedItem: View) {
        for (v: View in container.children) {
            val child = v as TextView
            if (child == selectedItem) {
                child.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.bg_round_rect)
                child.setTextColor(ContextCompat.getColor(requireContext(), R.color.iconic_dark))
            } else {
                child.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.bg_round_rect_gray)
                child.setTextColor(ContextCompat.getColor(requireContext(), R.color.hint))
            }
        }
    }

    private fun applyCovidDetail(covidData: List<CovidItem>): PieDataSet {
        val entries = ArrayList<PieEntry>()
        val total = covidData.first()
        entries.add(
            PieEntry(
                total.isolatingCount.toFloat(),
                getString(R.string.under_treatment)
            )
        )
        entries.add(
            PieEntry(
                total.isolationDoneCount.toFloat(),
                getString(R.string.complete_cure)
            )
        )
        entries.add(
            PieEntry(
                total.deathCount.toFloat(),
                getString(R.string.death)
            )
        )

        val dataSet = PieDataSet(entries, "")
        dataSet.setDrawIcons(false)
        dataSet.sliceSpace = 3f
        dataSet.iconsOffset = MPPointF(0f, 40f)
//        dataSet.selectionShift = 5f
        dataSet.selectionShift = 0f

        val colors = ArrayList<Int>()
        colors.add(ContextCompat.getColor(requireContext(), R.color.iconic_little_warn))
        colors.add(ContextCompat.getColor(requireContext(), R.color.iconic_safe))
        colors.add(ContextCompat.getColor(requireContext(), R.color.iconic_warn))
        dataSet.colors = colors
        return dataSet
    }

    private enum class PieType {
        Covid;
    }

    class DayValueFormatter(hashMap: HashMap<Float, Long>) : ValueFormatter() {
        private val data: HashMap<Float, Long> = hashMap
        private val calendar = Calendar.getInstance(Locale.getDefault())
        private val dayFormat = lazy { SimpleDateFormat("E", Locale.getDefault()) }
        private val timeFormat = lazy { SimpleDateFormat("HH'h'", Locale.getDefault()) }
        private var sectionInterval = 0
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            if (data.size > 1 && sectionInterval == 0) {
                if (data[0f].isNotNull() and data[1f].isNotNull()) {
                    val firstDateTime = data[0f]!!
                    val nextDate = data[1f]!!
                    sectionInterval = (nextDate - firstDateTime).toInt()
                }
            }
            val timeStamp = data[floor(value)]
            timeStamp.notNull {
                val additionalTimestamp = (sectionInterval * (value - floor(value))).toLong()
                val dateTimeResult = this + additionalTimestamp
                calendar.timeInMillis = dateTimeResult
                return if (calendar.get(Calendar.HOUR) == 0) {
                    dayFormat.value.format(calendar.time)
                } else {
                    timeFormat.value.format(calendar.time)
                }
            }
            return super.getAxisLabel(value, axis)
        }
    }
}