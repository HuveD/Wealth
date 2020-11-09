package kr.co.huve.wealth.view.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.jakewharton.rxrelay3.PublishRelay
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import kr.co.huve.wealth.R
import kr.co.huve.wealth.intent.WealthIntentFactory
import kr.co.huve.wealth.model.wealth.WealthModelStore
import kr.co.huve.wealth.model.wealth.WealthState
import kr.co.huve.wealth.util.WealthLocationManager
import kr.co.huve.wealth.view.EventObservable
import kr.co.huve.wealth.view.StateSubscriber
import kr.co.huve.wealth.view.main.CovidView
import kr.co.huve.wealth.view.main.WealthViewEvent
import kr.co.huve.wealth.view.main.adapter.WealthPagerAdapter
import timber.log.Timber
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
    private val format = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    private val calendar = Calendar.getInstance()

    private val requestRelay = PublishRelay.create<WealthViewEvent>()
    private val disposable = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        Timber.d("State Changed: $it")
        when (it) {
            is WealthState.FragmentSelected -> {
                if (it.position == WealthPagerAdapter.Companion.Type.Covid.ordinal) requestCovidData()
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
                    calendar.add(Calendar.DAY_OF_MONTH, -1)
                    requestCovidData()
                } else {
                    covidView.bind(it.data.reversed())
                    covidView.refreshProgress(false)
                }
            }
            is WealthState.CovidRequestFail -> {
                covidView.refreshProgress(false)
                Toast.makeText(
                    context,
                    getString(R.string.covid_request_fail),
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> Unit
        }
    }

    override fun events(): Observable<WealthViewEvent> = requestRelay

    private fun requestCovidData() {
        if (!covidView.isbinded) requestRelay.accept(
            WealthViewEvent.RequestCovid(
                format.format(
                    calendar.time
                )
            )
        )
    }
}