package kr.co.huve.wealth.view.main

import android.location.Geocoder
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import kr.co.huve.wealth.R
import kr.co.huve.wealth.model.backend.data.TotalWeather
import kr.co.huve.wealth.util.data.ExtraKey
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

class WealthActivity : AppCompatActivity() {

    private val disposables = CompositeDisposable()
    private var data: TotalWeather? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = ContextCompat.getColor(this, R.color.iconic_dark_blue)
        setContentView(R.layout.activity_main)
        currentStone.isEnabled = false
        data = intent.getSerializableExtra(ExtraKey.EXTRA_WEATHER_DATA) as TotalWeather?
        data?.run {
            // 도시
            Geocoder(this@WealthActivity, Locale.getDefault()).getFromLocation(lat, lon, 1)
                ?.run {
                    val stringBuilder = StringBuilder()
                    first().thoroughfare?.run { stringBuilder.append(this) }
                    city.text = stringBuilder.toString()
                }

            // 현재 날씨 아이콘 및 설명
            val element =
                if (current.weatherInfo.isNotEmpty()) current.weatherInfo.first() else null
            element?.run {
                this@WealthActivity.titleMessage.text =
                    getString(getWeatherDescription(this@WealthActivity.applicationContext))
                this@WealthActivity.titleImage.setImageResource(getWeatherIcon(true))
            }
            // 현재 온도
            titleTemp.text = "${current.temp.toInt()}"

            // 일출, 일몰
            sunSetTime.text = current.getTimeFromSunSetTime()
            sunRiseTime.text = current.getTimeFromSunRiseTime()
        }
        initializePredictWeatherList(true)

        todayTab.setOnClickListener {
            initializePredictWeatherList(true)
        }

        weekTab.setOnClickListener {
            initializePredictWeatherList(false)
        }
    }

    override fun onResume() {
        super.onResume()
        disposables.add(Observable.interval(0, 1, TimeUnit.MINUTES).subscribe {
            Timber.d(it.toString())
            invalidateCurrentStone()
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }

    private fun initializePredictWeatherList(isHourly: Boolean) {
        if (isHourly) {
            todayTab.setImageResource(R.drawable.img_tab_d)
            weekTab.setImageResource(R.drawable.img_tab)
            todayTab.elevation = 1f
            weekTab.elevation = 0f
            todayTabText.elevation = 1f
            weekTabText.elevation = 0f
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                todayTabText.setTextColor(this.resources.getColor(R.color.iconic_orange, null))
                weekTabText.setTextColor(this.resources.getColor(R.color.iconic_white, null))
            } else {
                todayTabText.setTextColor(this.resources.getColor(R.color.iconic_orange))
                weekTabText.setTextColor(this.resources.getColor(R.color.iconic_white))
            }
        } else {
            todayTab.setImageResource(R.drawable.img_tab)
            weekTab.setImageResource(R.drawable.img_tab_d)
            todayTab.elevation = 0f
            weekTab.elevation = 1f
            todayTabText.elevation = 0f
            weekTabText.elevation = 1f
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                todayTabText.setTextColor(this.resources.getColor(R.color.iconic_white, null))
                weekTabText.setTextColor(this.resources.getColor(R.color.iconic_orange, null))
            } else {
                todayTabText.setTextColor(this.resources.getColor(R.color.iconic_white))
                weekTabText.setTextColor(this.resources.getColor(R.color.iconic_orange))
            }
        }
        data?.run {
            val hourlyWeather = hourly
            val dailyWeather = daily
            predictWeatherList.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(this@WealthActivity).run {
                    orientation = LinearLayoutManager.HORIZONTAL
                    this
                }
                adapter = PredictWeatherAdapter(if (isHourly) hourlyWeather else dailyWeather)
            }
        }

    }

    private fun invalidateCurrentStone() {
        data?.run {
            val currentTime = Calendar.getInstance().timeInMillis
            val currentItem = current
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
}