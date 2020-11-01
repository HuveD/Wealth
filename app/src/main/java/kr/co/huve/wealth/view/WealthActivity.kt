package kr.co.huve.wealth.view

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
            name?.run { city.text = this }
            main?.run { temp?.run { this@WealthActivity.titleTemp.text = "${this}º" } }
            weather?.run { first().main?.run { this@WealthActivity.titleMessage.text = this } }
            sys?.run {
                sunset?.run {
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = this
                    sunSetTime.text =
                        "${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)}"
                }
                sunrise?.run {
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = this
                    sunRiseTime.text =
                        "${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)}"
                }
            }
        }
    }
}