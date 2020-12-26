package kr.co.huve.wealthApp.view.main.fragment

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.DialogFragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import dagger.hilt.android.AndroidEntryPoint
import kr.co.huve.wealthApp.R
import kr.co.huve.wealthApp.model.repository.data.CovidResult
import kr.co.huve.wealthApp.model.repository.data.DayWeather
import kr.co.huve.wealthApp.model.repository.data.TotalWeather
import kr.co.huve.wealthApp.model.repository.data.WeekWeather
import kr.co.huve.wealthApp.util.isNotNull
import kr.co.huve.wealthApp.util.isNull
import kr.co.huve.wealthApp.util.notNull
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.floor

private const val DEFAULT_LINE_WITH = 2.5f
private const val DEFAULT_TEXT_SIZE = 13f
private const val DEFAULT_CIRCLE_RADIUS = 4f
private const val DEFAULT_CUBIC_INTENSITY = 0.15f

@AndroidEntryPoint
class WeatherDetailDialog :
    DialogFragment() {

    private val axisHash = HashMap<Float, Long>()
    private var totalWeather: TotalWeather? = null
    private var covidResult: CovidResult? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val layout = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_detail, null)
            .apply {
                if (totalWeather.isNull()) {
                    dismiss()
                } else {
                    val tabContainer: ViewGroup = this.findViewById(R.id.tabContainer)
                    val chart: LineChart = this.findViewById(R.id.lineChartDetail)
                    initializeLineChart(chart, totalWeather!!)
                    this.findViewById<ImageView>(R.id.closeButton).setOnClickListener {
                        dismiss()
                    }
                    for (child: View in tabContainer.children) {
                        child.setOnClickListener {
                            when (it.id) {
                                R.id.detailTab -> invalidateChart(
                                    chart,
                                    LineType.WEATHER_DETAIL,
                                    totalWeather!!
                                )
                                R.id.weekTab -> invalidateChart(
                                    chart,
                                    LineType.WEEK_TEMP,
                                    totalWeather!!
                                )
                                R.id.rangeTab -> invalidateChart(
                                    chart,
                                    LineType.WEEK_RANGE,
                                    totalWeather!!
                                )
                                R.id.feelsLikeTab -> invalidateChart(
                                    chart,
                                    LineType.FEEL_TEMP,
                                    totalWeather!!
                                )
                            }
                            changeStyle(tabContainer, it)
                        }
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

    fun bind(totalWeather: TotalWeather?, covidResult: CovidResult? = null) {
        this.totalWeather = totalWeather
        this.covidResult = covidResult
    }

    private fun initializeLineChart(
        lineChart: LineChart,
        totalWeather: TotalWeather
    ) {
        if (axisHash.size > 0) axisHash.clear()
        lineChart.visibility = View.VISIBLE
        lineChart.apply {
            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
                textSize = DEFAULT_TEXT_SIZE
                xEntrySpace = 8f
                textColor = ContextCompat.getColor(context, R.color.iconic_dark)
            }
            xAxis.apply {
                textColor = ContextCompat.getColor(context, R.color.iconic_dark)
                textSize = DEFAULT_TEXT_SIZE
                position = XAxis.XAxisPosition.TOP
                extraTopOffset = 8f
                setAvoidFirstLastClipping(true)
                setDrawAxisLine(false)
                setDrawGridLines(false)
            }
            axisRight.apply {
                textColor = ContextCompat.getColor(context, R.color.iconic_dark)
                textSize = DEFAULT_TEXT_SIZE
                setDrawAxisLine(false)
                setDrawGridLines(false)
            }
            axisLeft.isEnabled = false
            setScaleEnabled(false)
            setTouchEnabled(false)

            isAutoScaleMinMaxEnabled = true
            description.isEnabled = false
        }
        invalidateChart(lineChart, LineType.WEATHER_DETAIL, totalWeather)
    }

    private fun invalidateChart(chart: LineChart, type: LineType, data: TotalWeather) {
        chart.apply {
            xAxis.valueFormatter = DayValueFormatter(axisHash)
            this.data = LineData(
                when (type) {
                    LineType.WEEK_TEMP -> applyWeekTemp(data)
                    LineType.WEEK_RANGE -> applyDailyTemperatureRange(data)
                    LineType.WEATHER_DETAIL -> applyWeatherDetail(data)
                    else -> applyFeelsTemp(data)
                }
            )
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

    private fun applyWeekTemp(totalWeather: TotalWeather): ArrayList<ILineDataSet> {
        var currentEntry = ArrayList<Entry>()
        var minEntry = ArrayList<Entry>()
        var maxEntry = ArrayList<Entry>()
        for (weather: WeekWeather in totalWeather.daily) {
            val position = (currentEntry.size).toFloat()
            val date = weather.dt * 1000
            currentEntry.add(Entry(position, weather.temp.day))
            minEntry.add(Entry(position, weather.temp.min))
            maxEntry.add(Entry(position, weather.temp.max))
            axisHash[position] = date
        }

        return ArrayList<ILineDataSet>().apply {
            add(LineDataSet(minEntry, "최저 기온").apply {
                mode = LineDataSet.Mode.LINEAR
                circleRadius = DEFAULT_CIRCLE_RADIUS
                lineWidth = DEFAULT_LINE_WITH
                valueTextSize = DEFAULT_TEXT_SIZE
                cubicIntensity = DEFAULT_CUBIC_INTENSITY
                color = ContextCompat.getColor(requireContext(), R.color.iconic_sky_blue)
                setCircleColor(ContextCompat.getColor(requireContext(), R.color.iconic_sky_blue))
                valueTextColor = ContextCompat.getColor(requireContext(), R.color.iconic_dark)
                setDrawValues(false)
            })
            add(LineDataSet(currentEntry, "평균 기온").apply {
                mode = LineDataSet.Mode.LINEAR
                circleRadius = DEFAULT_CIRCLE_RADIUS
                lineWidth = DEFAULT_LINE_WITH
                valueTextSize = DEFAULT_TEXT_SIZE
                cubicIntensity = DEFAULT_CUBIC_INTENSITY
                color = ContextCompat.getColor(requireContext(), R.color.iconic_dark)
                setCircleColor(ContextCompat.getColor(requireContext(), R.color.iconic_dark))
                circleHoleColor = ContextCompat.getColor(requireContext(), R.color.iconic_white)
                valueTextColor = ContextCompat.getColor(requireContext(), R.color.iconic_dark)
                setDrawValues(false)
            })
            add(LineDataSet(maxEntry, "최고 기온").apply {
                mode = LineDataSet.Mode.LINEAR
                valueTextSize = DEFAULT_TEXT_SIZE
                circleRadius = DEFAULT_CIRCLE_RADIUS
                cubicIntensity = DEFAULT_CUBIC_INTENSITY
                lineWidth = DEFAULT_LINE_WITH
                color = ContextCompat.getColor(requireContext(), R.color.iconic_red)
                setCircleColor(ContextCompat.getColor(requireContext(), R.color.iconic_red))
                valueTextColor = ContextCompat.getColor(requireContext(), R.color.iconic_dark)
                setDrawValues(false)
            })
        }
    }

    private fun applyDailyTemperatureRange(totalWeather: TotalWeather): ArrayList<ILineDataSet> {
        val rangeEntry = ArrayList<Entry>()
        for (weather: WeekWeather in totalWeather.daily) {
            val position = (rangeEntry.size).toFloat()
            val date = weather.dt * 1000
            rangeEntry.add(Entry(position, weather.temp.max - weather.temp.min))
            axisHash[position] = date
        }

        return ArrayList<ILineDataSet>().apply {
            add(LineDataSet(rangeEntry, "일교차").apply {
                mode = LineDataSet.Mode.LINEAR
                circleRadius = DEFAULT_CIRCLE_RADIUS
                lineWidth = DEFAULT_LINE_WITH
                valueTextSize = DEFAULT_TEXT_SIZE
                cubicIntensity = DEFAULT_CUBIC_INTENSITY
                color = ContextCompat.getColor(requireContext(), R.color.iconic_dark)
                setCircleColor(ContextCompat.getColor(requireContext(), R.color.iconic_dark))
                circleHoleColor = ContextCompat.getColor(requireContext(), R.color.iconic_white)
                valueTextColor = ContextCompat.getColor(requireContext(), R.color.iconic_dark)
                setDrawValues(false)
            })
        }
    }

    private fun applyFeelsTemp(totalWeather: TotalWeather): ArrayList<ILineDataSet> {
        var currentEntry = ArrayList<Entry>()
        val feelsEntry = ArrayList<Entry>()
        for (weather: DayWeather in totalWeather.hourly) {
            val position = (feelsEntry.size).toFloat()
            val date = weather.dt * 1000
            currentEntry.add(Entry(position, weather.temp))
            feelsEntry.add(Entry(position, weather.feelsLike))
            axisHash[position] = date
        }

        return ArrayList<ILineDataSet>().apply {
            add(LineDataSet(feelsEntry, "체감 온도").apply {
                mode = LineDataSet.Mode.LINEAR
                circleRadius = DEFAULT_CIRCLE_RADIUS
                lineWidth = DEFAULT_LINE_WITH
                valueTextSize = DEFAULT_TEXT_SIZE
                cubicIntensity = DEFAULT_CUBIC_INTENSITY
                color = ContextCompat.getColor(requireContext(), R.color.iconic_red)
                setCircleColor(ContextCompat.getColor(requireContext(), R.color.iconic_red))
                valueTextColor = ContextCompat.getColor(requireContext(), R.color.iconic_dark)
                setDrawValues(false)
            })
            add(LineDataSet(currentEntry, "평균 기온").apply {
                mode = LineDataSet.Mode.LINEAR
                circleRadius = DEFAULT_CIRCLE_RADIUS
                lineWidth = DEFAULT_LINE_WITH
                valueTextSize = DEFAULT_TEXT_SIZE
                cubicIntensity = DEFAULT_CUBIC_INTENSITY
                color = ContextCompat.getColor(requireContext(), R.color.iconic_dark)
                setCircleColor(ContextCompat.getColor(requireContext(), R.color.iconic_dark))
                circleHoleColor = ContextCompat.getColor(requireContext(), R.color.iconic_white)
                valueTextColor = ContextCompat.getColor(requireContext(), R.color.iconic_dark)
                setDrawValues(false)
            })
        }
    }

    private fun applyWeatherDetail(totalWeather: TotalWeather): ArrayList<ILineDataSet> {
        var humidityEntry = ArrayList<Entry>()
        val cloudyEntry = ArrayList<Entry>()
        val popEntry = ArrayList<Entry>()
        for (weather: DayWeather in totalWeather.hourly) {
            val position = (humidityEntry.size).toFloat()
            val date = weather.dt * 1000
            humidityEntry.add(Entry(position, weather.humidity.toFloat()))
            cloudyEntry.add(Entry(position, weather.clouds.toFloat()))
            popEntry.add(Entry(position, weather.pop ?: 0f))
            axisHash[position] = date
        }

        return ArrayList<ILineDataSet>().apply {
            add(LineDataSet(humidityEntry, "습도(%)").apply {
                mode = LineDataSet.Mode.LINEAR
                circleRadius = DEFAULT_CIRCLE_RADIUS
                lineWidth = DEFAULT_LINE_WITH
                valueTextSize = DEFAULT_TEXT_SIZE
                cubicIntensity = DEFAULT_CUBIC_INTENSITY
                color = ContextCompat.getColor(requireContext(), R.color.iconic_sky_blue)
                setCircleColor(ContextCompat.getColor(requireContext(), R.color.iconic_sky_blue))
                valueTextColor = ContextCompat.getColor(requireContext(), R.color.iconic_dark)
                setDrawValues(false)
            })
            add(LineDataSet(cloudyEntry, "운량(%)").apply {
                mode = LineDataSet.Mode.LINEAR
                circleRadius = DEFAULT_CIRCLE_RADIUS
                lineWidth = DEFAULT_LINE_WITH
                valueTextSize = DEFAULT_TEXT_SIZE
                cubicIntensity = DEFAULT_CUBIC_INTENSITY
                color = ContextCompat.getColor(requireContext(), R.color.iconic_little_warn)
                setCircleColor(ContextCompat.getColor(requireContext(), R.color.iconic_little_warn))
                circleHoleColor = ContextCompat.getColor(requireContext(), R.color.iconic_white)
                valueTextColor = ContextCompat.getColor(requireContext(), R.color.iconic_dark)
                setDrawValues(false)
            })
            add(LineDataSet(popEntry, "강수확률(%)").apply {
                mode = LineDataSet.Mode.LINEAR
                circleRadius = DEFAULT_CIRCLE_RADIUS
                lineWidth = DEFAULT_LINE_WITH
                valueTextSize = DEFAULT_TEXT_SIZE
                cubicIntensity = DEFAULT_CUBIC_INTENSITY
                color = ContextCompat.getColor(requireContext(), R.color.iconic_dark_blue)
                setCircleColor(ContextCompat.getColor(requireContext(), R.color.iconic_dark_blue))
                valueTextColor = ContextCompat.getColor(requireContext(), R.color.iconic_dark)
                setDrawValues(false)
            })
        }
    }

    private enum class LineType {
        WEEK_TEMP,
        WEEK_RANGE,
        FEEL_TEMP,
        WEATHER_DETAIL;
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