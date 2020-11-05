package kr.co.huve.wealth.view.main.fragment

import android.content.Context
import android.location.Geocoder
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.FragmentScoped
import kr.co.huve.wealth.R
import kr.co.huve.wealth.model.backend.data.TotalWeather
import kr.co.huve.wealth.view.main.adapter.PredictWeatherListAdapter
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@FragmentScoped
class WeatherView @Inject constructor(@ActivityContext val context: Context) {
    val view: View = LayoutInflater.from(context).inflate(R.layout.fragment_weather, null, false)
    private val predictWeatherList: RecyclerView
    private val currentStone: SeekBar
    private val titleMessage: TextView
    private val titleImage: ImageView
    private val titleTemp: TextView
    private val todayTab: ImageView
    private val todayTabText: TextView
    private val weekTab: ImageView
    private val weekTabText: TextView
    private val sunRiseTime: TextView
    private val sunSetTime: TextView
    private val city: TextView
    lateinit var totalWeather: TotalWeather

    init {
        predictWeatherList = view.findViewById(R.id.predictWeatherList)
        currentStone = view.findViewById(R.id.currentStone)
        titleMessage = view.findViewById(R.id.titleMessage)
        titleImage = view.findViewById(R.id.titleImage)
        titleTemp = view.findViewById(R.id.titleTemp)
        todayTab = view.findViewById(R.id.todayTab)
        todayTabText = view.findViewById(R.id.todayTabText)
        weekTab = view.findViewById(R.id.weekTab)
        weekTabText = view.findViewById(R.id.weekTabText)
        sunRiseTime = view.findViewById(R.id.sunRiseTime)
        sunSetTime = view.findViewById(R.id.sunSetTime)
        city = view.findViewById(R.id.city)
    }

    fun bind(data: TotalWeather) {
        this.totalWeather = data
        currentStone.isEnabled = false

        // 도시
        Geocoder(context, Locale.getDefault()).getFromLocation(
            totalWeather.lat,
            totalWeather.lon,
            1
        )?.run { city.text = first().thoroughfare }

        // 현재 날씨 아이콘 및 설명
        val current = totalWeather.current
        val element = if (current.weatherInfo.isNotEmpty()) current.weatherInfo.first() else null
        element?.run {
            titleMessage.text = context.getString(getWeatherDescription(context))
            titleImage.setImageResource(getWeatherIcon(true))
        }
        // 현재 온도
        titleTemp.text = "${current.temp.toInt()}"

        // 일출, 일몰
        sunSetTime.text = current.getTimeFromSunTime(current.sunset)
        sunRiseTime.text = current.getTimeFromSunTime(current.sunrise)

        initializePredictWeatherList(true)

        todayTab.setOnClickListener {
            initializePredictWeatherList(true)
        }

        weekTab.setOnClickListener {
            initializePredictWeatherList(false)
        }
    }

    private fun initializePredictWeatherList(isHourly: Boolean) {
        if (isHourly) {
            todayTab.setImageResource(R.drawable.img_tab_d)
            weekTab.setImageResource(R.drawable.img_tab)
            todayTab.elevation = 1f
            weekTab.elevation = 0f
            todayTabText.elevation = 1f
            weekTabText.elevation = 0f
            todayTabText.setTextColor(ContextCompat.getColor(context, R.color.iconic_orange))
            weekTabText.setTextColor(ContextCompat.getColor(context, R.color.iconic_white))
        } else {
            todayTab.setImageResource(R.drawable.img_tab)
            weekTab.setImageResource(R.drawable.img_tab_d)
            todayTab.elevation = 0f
            weekTab.elevation = 1f
            todayTabText.elevation = 0f
            weekTabText.elevation = 1f
            todayTabText.setTextColor(ContextCompat.getColor(context, R.color.iconic_white))
            weekTabText.setTextColor(ContextCompat.getColor(context, R.color.iconic_orange))
        }
        totalWeather.run {
            val calender = Calendar.getInstance()
            val hourlyWeather = hourly
            val dailyWeather = daily
            predictWeatherList.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(context).run {
                    orientation = LinearLayoutManager.HORIZONTAL
                    this
                }
                adapter =
                    PredictWeatherListAdapter(if (isHourly) hourlyWeather.filter {
                        it.dt * 1000L > calender.time.time
                    } else dailyWeather)
            }
        }
    }

    fun invalidateCurrentStone() {
        val currentTime = Calendar.getInstance().timeInMillis
        val currentItem = totalWeather.current
        val riseTime = currentItem.sunrise
        val setTime = currentItem.sunset
        if (riseTime != null && setTime != null) {
            currentStone.max = (setTime * 1000L - riseTime * 1000L).toInt()
            currentStone.progress = (currentTime - riseTime * 1000L).toInt()
            Timber.d((setTime * 1000L - riseTime * 1000L).toInt().toString())
            Timber.d((currentTime - (riseTime * 1000L)).toInt().toString())
        }
    }
}