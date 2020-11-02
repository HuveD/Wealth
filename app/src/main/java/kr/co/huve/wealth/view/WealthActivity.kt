package kr.co.huve.wealth.view

import android.location.Geocoder
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kr.co.huve.wealth.R
import kr.co.huve.wealth.model.backend.data.Weather
import kr.co.huve.wealth.util.data.ExtraKey
import java.util.*

class WealthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = ContextCompat.getColor(this, R.color.iconic_dark_blue)
        setContentView(R.layout.activity_main)

        var data = intent.getSerializableExtra(ExtraKey.EXTRA_WEATHER_DATA) as Weather?
        data?.run {
            coord?.run {
                val geocoder = Geocoder(this@WealthActivity, Locale.getDefault())
                geocoder.getFromLocation(lat, lon, 1)?.run {
                    val stringBuilder = StringBuilder()
                    first().thoroughfare?.run { stringBuilder.append(this) }
                    city.text = stringBuilder.toString()
                }
            }
            main?.run { temp?.run { this@WealthActivity.titleTemp.text = "${this.toInt()}" } }
            weather?.run {
                if (this.isNotEmpty()) {
                    val item = first()
                    item.description?.run {
                        this@WealthActivity.titleMessage.text =
                            getString(item.getWeatherDescription(this@WealthActivity.applicationContext))
                    }
                    item.id.run {
                        this@WealthActivity.titleImage.setImageResource(item.getWeatherIcon(true))
                    }
                }
            }
            sys?.run {
                sunSetTime.text = getTimeFromSunSetTime()
                sunRiseTime.text = getTimeFromSunRiseTime()
            }
        }
    }
}