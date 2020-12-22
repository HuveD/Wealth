package kr.co.huve.wealthApp.view

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.MPPointF
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.chart_test_activty.*
import kr.co.huve.wealthApp.R
import kr.co.huve.wealthApp.model.repository.data.*
import kr.co.huve.wealthApp.model.repository.network.NetworkConfig
import kr.co.huve.wealthApp.model.repository.network.layer.CovidRestApi
import kr.co.huve.wealthApp.util.isNotNull
import kr.co.huve.wealthApp.util.notNull
import org.json.XML
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.floor

private const val DEFAULT_LINE_WITH = 2.5f
private const val DEFAULT_TEXT_SIZE = 13f
private const val DEFAULT_CIRCLE_RADIUS = 4f
private const val DEFAULT_CUBIC_INTENSITY = 0.15f

@AndroidEntryPoint
class TestActivity : AppCompatActivity() {
    @Inject
    lateinit var covidApi: CovidRestApi

    @Inject
    lateinit var gson: Gson

    private val format = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    private val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1) }
    private val axisHash = HashMap<Float, Long>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chart_test_activty)

        val weatherData = intent.getSerializableExtra(DataKey.EXTRA_WEATHER_DATA.name)
        covidApi.getCovidStatus(
            NetworkConfig.COVID_KEY,
            1,
            20,
            format.format(calendar.time),
            format.format(calendar.time)
        ).observeOn(AndroidSchedulers.mainThread()).subscribe {
            val result = gson.fromJson(XML.toJSONObject(it).toString(), CovidResult::class.java)
            if (weatherData.isNotNull() and result.isNotNull()) {
                buttonContainer.apply {
                    for (type: LineType in LineType.values()) {
                        addView(Button(context).apply {
                            setOnClickListener {
                                initializeLineChart(type, weatherData as TotalWeather)
                            }
                            text = type.name
                        })
                    }
                    for (type: PieType in PieType.values()) {
                        addView(Button(context).apply {
                            setOnClickListener {
                                initializePieChart(type, result.getItemList())
                            }
                            text = type.name
                        })
                    }
                }
            }
        }
    }

    private fun initializeLineChart(type: LineType, totalWeather: TotalWeather) {
        if (axisHash.size > 0) axisHash.clear()
        pieChart.visibility = View.GONE
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
                valueFormatter = when (type) {
                    LineType.FEEL_TEMP, LineType.DAY_TEMP, LineType.WEEK_TEMP, LineType.WEATHER_DETAIL -> {
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
                    LineType.WEEK_TEMP -> applyWeekTemp(totalWeather)
                    LineType.DAY_TEMP -> applyDailyTemperatureRange(totalWeather)
                    LineType.WEATHER_DETAIL -> applyWeatherDetail(totalWeather)
                    else -> applyFeelsTemp(totalWeather)
                }
            )
            invalidate()
        }
    }

    private fun initializePieChart(type: PieType, covidItem: List<CovidItem>) {
        if (axisHash.size > 0) axisHash.clear()
        lineChart.visibility = View.GONE
        pieChart.visibility = View.VISIBLE
        pieChart.apply {
            // chart.spin(2000, 0, 360);
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
            setEntryLabelColor(ContextCompat.getColor(this@TestActivity, R.color.iconic_white))
            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(110)
            setHoleColor(Color.WHITE)

            holeRadius = 58f
            transparentCircleRadius = 61f

            centerText = covidItem.reversed().first().region
            setCenterTextTypeface(Typeface.DEFAULT_BOLD)
            setDrawCenterText(true)

            rotationAngle = 0f
            isRotationEnabled = false
            isHighlightPerTapEnabled = true

            data = PieData(
                when (type) {
                    PieType.Covid -> applyCovidDetail(covidItem.reversed())
                }
            )
            // entry label styling
            setEntryLabelColor(ContextCompat.getColor(this@TestActivity, R.color.iconic_dark))
            setEntryLabelTextSize(DEFAULT_TEXT_SIZE)
            setDrawEntryLabels(false)
            // value styling
            data.setValueTextColor(ContextCompat.getColor(this@TestActivity, R.color.iconic_dark))
            data.setValueFormatter(PercentFormatter(this))
            data.setValueTextSize(DEFAULT_TEXT_SIZE)
            data.setValueTypeface(Typeface.DEFAULT_BOLD)
            highlightValues(emptyArray())
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

    private fun applyCovidDetail(covidData: List<CovidItem>): PieDataSet {
        val entries = ArrayList<PieEntry>()
        val total = covidData.first()
        entries.add(
            PieEntry(
                total.isolatingCount.toFloat(),
                "치료"
            )
        )
        entries.add(
            PieEntry(
                total.isolationDoneCount.toFloat(),
                "완치"
            )
        )
        entries.add(
            PieEntry(
                total.deathCount.toFloat(),
                "사망"
            )
        )

        val dataSet = PieDataSet(entries, "")
        dataSet.setDrawIcons(false)
        dataSet.sliceSpace = 3f
        dataSet.iconsOffset = MPPointF(0f, 40f)
//        dataSet.selectionShift = 5f
        dataSet.selectionShift = 0f

        val colors = ArrayList<Int>()
        colors.add(ContextCompat.getColor(this, R.color.iconic_little_warn))
        colors.add(ContextCompat.getColor(this, R.color.iconic_safe))
        colors.add(ContextCompat.getColor(this, R.color.iconic_warn))
        dataSet.colors = colors
        return dataSet
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

private enum class LineType {
    WEEK_TEMP,
    DAY_TEMP,
    FEEL_TEMP,
    WEATHER_DETAIL;
}

private enum class PieType {
    Covid;
}