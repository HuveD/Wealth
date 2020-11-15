package kr.co.huve.wealthApp.view.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jakewharton.rxbinding4.view.clicks
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import kr.co.huve.wealthApp.intent.WealthIntentFactory
import kr.co.huve.wealthApp.model.backend.data.TotalWeather
import kr.co.huve.wealthApp.model.wealth.WealthModelStore
import kr.co.huve.wealthApp.model.wealth.WealthState
import kr.co.huve.wealthApp.util.data.DataKey
import kr.co.huve.wealthApp.view.EventObservable
import kr.co.huve.wealthApp.view.StateSubscriber
import kr.co.huve.wealthApp.view.main.WealthViewEvent
import kr.co.huve.wealthApp.view.main.WeatherView
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class WeatherFragment : Fragment(), EventObservable<WealthViewEvent>, StateSubscriber<WealthState> {

    @Inject
    lateinit var weatherView: WeatherView

    @Inject
    lateinit var intentFactory: WealthIntentFactory

    @Inject
    lateinit var modelStore: WealthModelStore

    private val disposables = CompositeDisposable()
    private var data: TotalWeather? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        data = activity?.run {
            intent.getSerializableExtra(DataKey.EXTRA_WEATHER_DATA.name) as TotalWeather?
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        data?.apply { weatherView.bind(this) }
        return weatherView.view
    }

    override fun onResume() {
        super.onResume()
        disposables.add(events().subscribe(intentFactory::process))
        disposables.add(modelStore.modelState().subscribeToState())
    }

    override fun onPause() {
        super.onPause()
        disposables.clear()
    }

    override fun events(): Observable<WealthViewEvent> {
        return Observable.merge(
            Observable.interval(0, 1, TimeUnit.MINUTES).map { WealthViewEvent.InvalidateStone },
            weatherView.weekTab.clicks().map { WealthViewEvent.WeatherTabChanged(isHour = false) },
            weatherView.todayTab.clicks().map { WealthViewEvent.WeatherTabChanged(isHour = true) }
        )
    }

    override fun Observable<WealthState>.subscribeToState(): Disposable {
        return subscribe {
            Timber.d("State Changed: $it")
            when (it) {
                WealthState.InvalidateStone -> weatherView.invalidateCurrentStone()
                is WealthState.WeatherTabChanged -> {
                    weatherView.initializePredictWeatherList(it.isHour)
                }
                else -> Unit
            }
        }
    }
}