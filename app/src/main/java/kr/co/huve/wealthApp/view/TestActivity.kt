package kr.co.huve.wealthApp.view

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import kotlinx.android.synthetic.main.chart_test_activty.*
import kr.co.huve.wealthApp.R
import kr.co.huve.wealthApp.model.repository.data.DataKey
import kr.co.huve.wealthApp.model.repository.data.DayWeather
import kr.co.huve.wealthApp.model.repository.data.TotalWeather
import kr.co.huve.wealthApp.model.repository.data.WeekWeather
import kr.co.huve.wealthApp.util.notNull
import kr.co.huve.wealthApp.util.notNulls
import kr.co.huve.wealthApp.util.whenNull
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.floor

private const val DEFAULT_LINE_WITH = 2.5f
private const val DEFAULT_TEXT_SIZE = 13f
private const val DEFAULT_CIRCLE_RADIUS = 4f
private const val DEFAULT_CUBIC_INTENSITY = 0.15f

class TestActivity : AppCompatActivity() {
    private val axisHash = HashMap<Float, Long>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chart_test_activty)
        intent.getSerializableExtra(DataKey.EXTRA_WEATHER_DATA.name).notNull {
            buttonContainer.apply {
                for (type: CharType in CharType.values()) {
                    addView(Button(context).apply {
                        setOnClickListener {
                            initializeLineChart(type, this@notNull as TotalWeather)
                        }
                        text = type.name
                    })
                }
            }
        }.whenNull {
            Toast.makeText(this, "no data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeLineChart(type: CharType, totalWeather: TotalWeather) {
        if (axisHash.size > 0) axisHash.clear()
        chart.apply {
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
                valueFormatter = when (type) {
                    CharType.FEEL_TEMP, CharType.DAY_TEMP, CharType.WEEK_TEMP, CharType.WEATHER_DETAIL -> {
                        DayValueFormatter(axisHash)
                    }
                    else -> null
                }
                textColor = ContextCompat.getColor(context, R.color.iconic_dark)
                textSize = DEFAULT_TEXT_SIZE
                position = XAxisPosition.TOP
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

            data = LineData(
                when (type) {
                    CharType.WEEK_TEMP -> applyWeekTemp(totalWeather)
                    CharType.DAY_TEMP -> applyDailyTemperatureRange(totalWeather)
                    CharType.WEATHER_DETAIL -> applyWeatherDetail(totalWeather)
                    else -> applyFeelsTemp(totalWeather)
                }
            )
            invalidate()
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
                color = ContextCompat.getColor(baseContext, R.color.iconic_sky_blue)
                setCircleColor(ContextCompat.getColor(baseContext, R.color.iconic_sky_blue))
                valueTextColor = ContextCompat.getColor(baseContext, R.color.iconic_dark)
            })
            add(LineDataSet(currentEntry, "평균 기온").apply {
                mode = LineDataSet.Mode.LINEAR
                circleRadius = DEFAULT_CIRCLE_RADIUS
                lineWidth = DEFAULT_LINE_WITH
                valueTextSize = DEFAULT_TEXT_SIZE
                cubicIntensity = DEFAULT_CUBIC_INTENSITY
                color = ContextCompat.getColor(baseContext, R.color.iconic_dark)
                setCircleColor(ContextCompat.getColor(baseContext, R.color.iconic_dark))
                circleHoleColor = ContextCompat.getColor(baseContext, R.color.iconic_white)
                valueTextColor = ContextCompat.getColor(baseContext, R.color.iconic_dark)
            })
            add(LineDataSet(maxEntry, "최고 기온").apply {
                mode = LineDataSet.Mode.LINEAR
                valueTextSize = DEFAULT_TEXT_SIZE
                circleRadius = DEFAULT_CIRCLE_RADIUS
                cubicIntensity = DEFAULT_CUBIC_INTENSITY
                lineWidth = DEFAULT_LINE_WITH
                color = ContextCompat.getColor(baseContext, R.color.iconic_red)
                setCircleColor(ContextCompat.getColor(baseContext, R.color.iconic_red))
                valueTextColor = ContextCompat.getColor(baseContext, R.color.iconic_dark)
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
                color = ContextCompat.getColor(baseContext, R.color.iconic_dark)
                setCircleColor(ContextCompat.getColor(baseContext, R.color.iconic_dark))
                circleHoleColor = ContextCompat.getColor(baseContext, R.color.iconic_white)
                valueTextColor = ContextCompat.getColor(baseContext, R.color.iconic_dark)

                setDrawFilled(true)
                fillColor = ContextCompat.getColor(baseContext, R.color.white)
                fillAlpha = 100
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
                color = ContextCompat.getColor(baseContext, R.color.iconic_red)
                setCircleColor(ContextCompat.getColor(baseContext, R.color.iconic_red))
                valueTextColor = ContextCompat.getColor(baseContext, R.color.iconic_dark)
            })
            add(LineDataSet(currentEntry, "평균 기온").apply {
                mode = LineDataSet.Mode.LINEAR
                circleRadius = DEFAULT_CIRCLE_RADIUS
                lineWidth = DEFAULT_LINE_WITH
                valueTextSize = DEFAULT_TEXT_SIZE
                cubicIntensity = DEFAULT_CUBIC_INTENSITY
                color = ContextCompat.getColor(baseContext, R.color.iconic_dark)
                setCircleColor(ContextCompat.getColor(baseContext, R.color.iconic_dark))
                circleHoleColor = ContextCompat.getColor(baseContext, R.color.iconic_white)
                valueTextColor = ContextCompat.getColor(baseContext, R.color.iconic_dark)
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
                color = ContextCompat.getColor(baseContext, R.color.iconic_sky_blue)
                setCircleColor(ContextCompat.getColor(baseContext, R.color.iconic_sky_blue))
                valueTextColor = ContextCompat.getColor(baseContext, R.color.iconic_dark)
            })
            add(LineDataSet(cloudyEntry, "운량(%)").apply {
                mode = LineDataSet.Mode.LINEAR
                circleRadius = DEFAULT_CIRCLE_RADIUS
                lineWidth = DEFAULT_LINE_WITH
                valueTextSize = DEFAULT_TEXT_SIZE
                cubicIntensity = DEFAULT_CUBIC_INTENSITY
                color = ContextCompat.getColor(baseContext, R.color.iconic_little_warn)
                setCircleColor(ContextCompat.getColor(baseContext, R.color.iconic_little_warn))
                circleHoleColor = ContextCompat.getColor(baseContext, R.color.iconic_white)
                valueTextColor = ContextCompat.getColor(baseContext, R.color.iconic_dark)
            })
            add(LineDataSet(popEntry, "강수확률(%)").apply {
                mode = LineDataSet.Mode.LINEAR
                circleRadius = DEFAULT_CIRCLE_RADIUS
                lineWidth = DEFAULT_LINE_WITH
                valueTextSize = DEFAULT_TEXT_SIZE
                cubicIntensity = DEFAULT_CUBIC_INTENSITY
                color = ContextCompat.getColor(baseContext, R.color.iconic_dark_blue)
                setCircleColor(ContextCompat.getColor(baseContext, R.color.iconic_dark_blue))
                valueTextColor = ContextCompat.getColor(baseContext, R.color.iconic_dark)
            })
        }
    }
}

class DayValueFormatter(hashMap: HashMap<Float, Long>) : ValueFormatter() {
    private val data: HashMap<Float, Long> = hashMap
    private val calendar = Calendar.getInstance(Locale.getDefault())
    private val dayFormat = lazy { SimpleDateFormat("E", Locale.getDefault()) }
    private val timeFormat = lazy { SimpleDateFormat("HH'h'", Locale.getDefault()) }
    private var sectionInterval = 0
    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        if (data.size > 1 && sectionInterval == 0) {
            notNulls(data[0f], data[1f]) {
                val firstDateTime = it[0]
                val nextDate = it[1]
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

private enum class CharType {
    WEEK_TEMP,
    DAY_TEMP,
    FEEL_TEMP,
    WEATHER_DETAIL;
}