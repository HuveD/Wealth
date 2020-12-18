package kr.co.huve.wealthApp.view

import android.graphics.Paint
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Description
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

class TestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chart_test_activty)

        intent.getSerializableExtra(DataKey.EXTRA_WEATHER_DATA.name).notNull {
            initializeChart(this as TotalWeather)
        }.whenNull {
            Toast.makeText(this, "no data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeChart(totalWeather: TotalWeather) {
        var dataSet = ArrayList<ILineDataSet>()
        var currentEntry = ArrayList<Entry>()
        var minEntry = ArrayList<Entry>()
        var maxEntry = ArrayList<Entry>()

        val axisHash = HashMap<Float, Long>()
        for (weather: WeekWeather in totalWeather.daily) {
            val position = (currentEntry.size + 1).toFloat()
            val date = weather.dt * 1000
            currentEntry.add(Entry(position, weather.temp.day))
            minEntry.add(Entry(position, weather.temp.min))
            maxEntry.add(Entry(position, weather.temp.max))
            axisHash[position] = date
        }

        dataSet.apply {
            add(LineDataSet(minEntry, "최저 기온").apply {
                circleRadius = 4f
                color = ContextCompat.getColor(baseContext, R.color.iconic_sky_blue)
                setCircleColor(ContextCompat.getColor(baseContext, R.color.iconic_sky_blue))
                lineWidth = 2.5f
                valueTextSize = 15f
                valueTextColor = ContextCompat.getColor(baseContext, R.color.iconic_white)

            })
            add(LineDataSet(currentEntry, "평균 기온").apply {
                circleRadius = 4f
                lineWidth = 2.5f
                color = ContextCompat.getColor(baseContext, R.color.iconic_white)
                setCircleColor(ContextCompat.getColor(baseContext, R.color.iconic_white))
                circleHoleColor = ContextCompat.getColor(baseContext, R.color.iconic_dark)
                valueTextSize = 15f
                valueTextColor = ContextCompat.getColor(baseContext, R.color.iconic_white)
            })
            add(LineDataSet(maxEntry, "최고 기온").apply {
                circleRadius = 4f
                lineWidth = 2.5f
                color = ContextCompat.getColor(baseContext, R.color.iconic_red)
                setCircleColor(ContextCompat.getColor(baseContext, R.color.iconic_red))
                valueTextSize = 15f
                valueTextColor = ContextCompat.getColor(baseContext, R.color.iconic_white)
            })
        }

        chart.apply {
            legend.textSize = 15f
            legend.textColor = ContextCompat.getColor(context, R.color.iconic_white)

            xAxis.valueFormatter = DayValueFormatter(axisHash)
            axisLeft.isEnabled = false
            axisRight.isEnabled = false

//            axisRight.setDrawAxisLine(false)
//            axisRight.setDrawGridLines(false)
            xAxis.setAvoidFirstLastClipping(true)
            xAxis.setDrawAxisLine(false)
            xAxis.setDrawGridLines(false)
            xAxis.textColor = ContextCompat.getColor(context, R.color.iconic_white)
            xAxis.textSize = 15f
            xAxis.position = XAxisPosition.TOP_INSIDE

            description = Description().apply {
                text = "Week Weather"
                textColor = ContextCompat.getColor(context, R.color.iconic_white)
                textAlign = Paint.Align.RIGHT
                textSize = 15f
            }

//            setTouchEnabled(false)
            data = LineData(dataSet)
            isAutoScaleMinMaxEnabled = true
            background = ContextCompat.getDrawable(baseContext, R.drawable.bg_good)
            setScaleEnabled(false)
            invalidate()
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