package kr.co.huve.wealthApp.view.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.jakewharton.rxbinding4.view.clicks
import com.jakewharton.rxrelay3.PublishRelay
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import kr.co.huve.wealthApp.R
import kr.co.huve.wealthApp.intent.WealthIntentFactory
import kr.co.huve.wealthApp.model.repository.data.CovidItem
import kr.co.huve.wealthApp.model.wealth.WealthModelStore
import kr.co.huve.wealthApp.model.wealth.WealthState
import kr.co.huve.wealthApp.util.WealthLocationManager
import kr.co.huve.wealthApp.view.EventObservable
import kr.co.huve.wealthApp.view.StateSubscriber
import kr.co.huve.wealthApp.view.main.CovidView
import kr.co.huve.wealthApp.view.main.WealthViewEvent
import kr.co.huve.wealthApp.view.main.adapter.WealthPagerAdapter
import kr.co.huve.wealthApp.view.main.dialog.CovidDetailDialog
import kr.co.huve.wealthApp.view.main.dialog.WeatherDetailDialog
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class CovidFragment : Fragment(), StateSubscriber<WealthState>, EventObservable<WealthViewEvent> {

    @Inject
    lateinit var locationManager: WealthLocationManager

    @Inject
    lateinit var modelStore: WealthModelStore

    @Inject
    lateinit var intentFactory: WealthIntentFactory

    @Inject
    lateinit var covidView: CovidView

    private lateinit var covidData: List<CovidItem>
    private val format = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    private val calendar = Calendar.getInstance()

    private val relay = PublishRelay.create<WealthViewEvent>()
    private val disposable = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return covidView.view
    }

    override fun onResume() {
        super.onResume()
        disposable.add(events().subscribe(intentFactory::process))
        disposable.add(modelStore.modelState().subscribeToState())
    }

    override fun onPause() {
        super.onPause()
        disposable.clear()
    }

    override fun Observable<WealthState>.subscribeToState(): Disposable = subscribe {
        when (it) {
            is WealthState.FragmentSelected -> {
                if (it.position == WealthPagerAdapter.Type.Covid.ordinal) requestCovidData()
            }
            is WealthState.RequestCovid -> {
                disposable.add(it.disposable)
                covidView.refreshProgress(show = true)
            }
            is WealthState.CovidDataReceived -> {
                if (it.data.isEmpty()) {
                    Toast.makeText(
                        context,
                        getString(R.string.not_exist_update_data),
                        Toast.LENGTH_SHORT
                    ).show()

                    // 하루 전 데이터 요청
                    calendar.add(Calendar.DAY_OF_MONTH, -1)
                    requestCovidData()
                } else {
                    covidData = it.data.reversed()
                    covidView.bind(covidData, onListItemClicked)
                    covidView.refreshProgress(false)
                }
            }
            is WealthState.FailReceiveResponseFromAPI -> {
                covidView.refreshProgress(false)
                Toast.makeText(
                    context,
                    getString(R.string.covid_request_fail),
                    Toast.LENGTH_SHORT
                ).show()
            }
            is WealthState.RefreshCovidDashboard -> {
                covidView.invalidateData(it.item)
            }
            is WealthState.ShowDetail -> showDetailPopup()
            else -> Unit
        }
    }

    override fun events(): Observable<WealthViewEvent> =
        Observable.merge(relay, covidView.detail.clicks().map { WealthViewEvent.ShowDetail })

    private fun requestCovidData() {
        if (!covidView.isbinded) relay.accept(
            WealthViewEvent.RequestCovid(
                format.format(
                    calendar.time
                )
            )
        )
    }

    private fun showDetailPopup() {
        CovidDetailDialog()
            .apply { bind(covidData) }
            .show(parentFragmentManager, WeatherDetailDialog::class.simpleName)
    }

    private val onListItemClicked = { item: CovidItem ->
        relay.accept(WealthViewEvent.RefreshCovidDashboard(item))
    }
}