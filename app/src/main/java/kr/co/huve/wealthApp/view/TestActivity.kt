package kr.co.huve.wealthApp.view

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import kotlinx.android.synthetic.main.chart_test_activty.*
import kr.co.huve.wealthApp.R
import kr.co.huve.wealthApp.model.repository.data.DataKey
import kr.co.huve.wealthApp.model.repository.data.TotalWeather
import kr.co.huve.wealthApp.model.repository.data.WeekWeather
import kr.co.huve.wealthApp.util.notNull
import kr.co.huve.wealthApp.util.whenNull
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

private const val DEFAULT_LINE_WITH = 2.5f
private const val DEFAULT_TEXT_SIZE = 15f
private const val DEFAULT_CIRCLE_RADIUS = 4f
private const val DEFAULT_CUBIC_INTENSITY = 0.15f

class TestActivity : AppCompatActivity() {
    val axisHash = HashMap<Float, Long>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chart_test_activty)

        intent.getSerializableExtra(DataKey.EXTRA_WEATHER_DATA.name).notNull {
            initializeChart(CharType.DAY_TEMP, this as TotalWeather)
        }.whenNull {
            Toast.makeText(this, "no data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeChart(type: CharType, totalWeather: TotalWeather) {
        if (axisHash.size > 0) axisHash.clear()
        chart.apply {
            legend.textSize = DEFAULT_TEXT_SIZE
            legend.xEntrySpace = 8f
            legend.textColor = ContextCompat.getColor(context, R.color.iconic_white)

            xAxis.valueFormatter = DayValueFormatter(axisHash)
            axisLeft.isEnabled = false
            axisRight.isEnabled = false

            xAxis.setDrawAxisLine(false)
            xAxis.setDrawGridLines(false)
            xAxis.textColor = ContextCompat.getColor(context, R.color.iconic_white)
            xAxis.textSize = DEFAULT_TEXT_SIZE
            xAxis.position = XAxisPosition.TOP_INSIDE

            setTouchEnabled(false)
            data = LineData(
                when (type) {
                    CharType.WEEK_TEMP -> applyWeekTemp(totalWeather)
                    CharType.DAY_TEMP -> applyDailyTemperatureRange(totalWeather)
                    else -> applyWeekTemp(totalWeather)
                }
            )
            isAutoScaleMinMaxEnabled = true
            background = ContextCompat.getDrawable(baseContext, R.drawable.bg_good)
            description.isEnabled = false
            setScaleEnabled(false)
            invalidate()
        }
    }

    private fun applyWeekTemp(totalWeather: TotalWeather): ArrayList<ILineDataSet> {
        var currentEntry = ArrayList<Entry>()
        var minEntry = ArrayList<Entry>()
        var maxEntry = ArrayList<Entry>()
        for (weather: WeekWeather in totalWeather.daily) {
            val position = (currentEntry.size + 1).toFloat()
            val date = weather.dt * 1000
            currentEntry.add(Entry(position, weather.temp.day))
            minEntry.add(Entry(position, weather.temp.min))
            maxEntry.add(Entry(position, weather.temp.max))
            axisHash[position] = date
        }

        return ArrayList<ILineDataSet>().apply {
            add(LineDataSet(minEntry, "최저 기온").apply {
                mode = LineDataSet.Mode.CUBIC_BEZIER
                circleRadius = DEFAULT_CIRCLE_RADIUS
                lineWidth = DEFAULT_LINE_WITH
                valueTextSize = DEFAULT_TEXT_SIZE
                cubicIntensity = DEFAULT_CUBIC_INTENSITY
                color = ContextCompat.getColor(baseContext, R.color.iconic_sky_blue)
                setCircleColor(ContextCompat.getColor(baseContext, R.color.iconic_sky_blue))
                valueTextColor = ContextCompat.getColor(baseContext, R.color.iconic_white)
            })
            add(LineDataSet(currentEntry, "평균 기온").apply {
                mode = LineDataSet.Mode.CUBIC_BEZIER
                circleRadius = DEFAULT_CIRCLE_RADIUS
                lineWidth = DEFAULT_LINE_WITH
                valueTextSize = DEFAULT_TEXT_SIZE
                cubicIntensity = DEFAULT_CUBIC_INTENSITY
                color = ContextCompat.getColor(baseContext, R.color.iconic_white)
                setCircleColor(ContextCompat.getColor(baseContext, R.color.iconic_white))
                circleHoleColor = ContextCompat.getColor(baseContext, R.color.iconic_dark)
                valueTextColor = ContextCompat.getColor(baseContext, R.color.iconic_white)
            })
            add(LineDataSet(maxEntry, "최고 기온").apply {
                mode = LineDataSet.Mode.CUBIC_BEZIER
                valueTextSize = DEFAULT_TEXT_SIZE
                circleRadius = DEFAULT_CIRCLE_RADIUS
                cubicIntensity = DEFAULT_CUBIC_INTENSITY
                lineWidth = DEFAULT_LINE_WITH
                color = ContextCompat.getColor(baseContext, R.color.iconic_red)
                setCircleColor(ContextCompat.getColor(baseContext, R.color.iconic_red))
                valueTextColor = ContextCompat.getColor(baseContext, R.color.iconic_white)
            })
        }
    }

    private fun applyDailyTemperatureRange(totalWeather: TotalWeather): ArrayList<ILineDataSet> {
        var rangeEntry = ArrayList<Entry>()
        for (weather: WeekWeather in totalWeather.daily) {
            val position = (rangeEntry.size).toFloat()
            val date = weather.dt * 1000
            rangeEntry.add(Entry(position, weather.temp.max - weather.temp.min))
            axisHash[position] = date
        }

        return ArrayList<ILineDataSet>().apply {
            add(LineDataSet(rangeEntry, "일교차").apply {
                mode = LineDataSet.Mode.CUBIC_BEZIER
                circleRadius = DEFAULT_CIRCLE_RADIUS
                lineWidth = DEFAULT_LINE_WITH
                valueTextSize = DEFAULT_TEXT_SIZE
                cubicIntensity = DEFAULT_CUBIC_INTENSITY
                color = ContextCompat.getColor(baseContext, R.color.iconic_white)
                setCircleColor(ContextCompat.getColor(baseContext, R.color.iconic_white))
                circleHoleColor = ContextCompat.getColor(baseContext, R.color.iconic_dark)
                valueTextColor = ContextCompat.getColor(baseContext, R.color.iconic_white)
            })
        }
    }
}

class DayValueFormatter(hashMap: HashMap<Float, Long>) : ValueFormatter() {
    val data: HashMap<Float, Long> = hashMap
    private val simpleDate = lazy { SimpleDateFormat("E", Locale.getDefault()) }
    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        data[value].notNull {
            return simpleDate.value.format(Date(this))
        }
        return super.getAxisLabel(value, axis)
    }
}

private enum class CharType {
    WEEK_TEMP,
    DAY_TEMP
}