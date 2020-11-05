package kr.co.huve.wealth.view.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kr.co.huve.wealth.model.backend.data.TotalWeather
import kr.co.huve.wealth.util.data.ExtraKey
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class WeatherFragment : Fragment() {

    @Inject
    lateinit var weatherView: WeatherView
    private val disposables = CompositeDisposable()
    private var data: TotalWeather? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        data = activity?.run {
            intent.getSerializableExtra(ExtraKey.EXTRA_WEATHER_DATA) as TotalWeather?
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        data?.run { weatherView.bind(this) }
        return weatherView.view
    }

    override fun onResume() {
        super.onResume()
        disposables.add(Observable.interval(0, 1, TimeUnit.MINUTES).subscribe {
            weatherView.invalidateCurrentStone()
        })
    }

    override fun onPause() {
        super.onPause()
        disposables.clear()
    }
}