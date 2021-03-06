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
import kr.co.huve.wealthApp.model.repository.data.DataKey
import kr.co.huve.wealthApp.model.repository.data.TotalWeather
import kr.co.huve.wealthApp.model.wealth.WealthModelStore
import kr.co.huve.wealthApp.model.wealth.WealthState
import kr.co.huve.wealthApp.view.EventObservable
import kr.co.huve.wealthApp.view.StateSubscriber
import kr.co.huve.wealthApp.view.main.WealthViewEvent
import kr.co.huve.wealthApp.view.main.WeatherView
import kr.co.huve.wealthApp.view.main.dialog.WeatherDetailDialog
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
        val bundle = savedInstanceState ?: requireActivity().intent.extras
        data = bundle?.getSerializable(DataKey.EXTRA_WEATHER_DATA.name) as TotalWeather?
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(DataKey.EXTRA_WEATHER_DATA.name, data)
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
            weatherView.todayTab.clicks().map { WealthViewEvent.WeatherTabChanged(isHour = true) },
            weatherView.detail.clicks().map { WealthViewEvent.ShowDetail }
        )
    }

    override fun Observable<WealthState>.subscribeToState(): Disposable {
        return subscribe {
            when (it) {
                WealthState.ShowDetail -> showDetailPopup()
                WealthState.InvalidateStone -> weatherView.invalidateCurrentStone()
                is WealthState.WeatherTabChanged -> {
                    weatherView.initializePredictWeatherList(it.isHour)
                }
                else -> Unit
            }
        }
    }

    private fun showDetailPopup() {
        WeatherDetailDialog()
            .apply { bind(data) }
            .show(parentFragmentManager, WeatherDetailDialog::class.simpleName)
    }
}