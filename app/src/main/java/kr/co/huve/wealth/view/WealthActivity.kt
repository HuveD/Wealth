package kr.co.huve.wealth.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kr.co.huve.wealth.R
import kr.co.huve.wealth.model.backend.data.Weather
import kr.co.huve.wealth.util.data.ExtraKey
import timber.log.Timber

class WealthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var data = intent.getSerializableExtra(ExtraKey.EXTRA_WEATHER_DATA) as Weather
        Timber.tag("정현")
        Timber.d(data.toString())
        Timber.d(data.toString())
        Timber.d(data.toString())
    }
}