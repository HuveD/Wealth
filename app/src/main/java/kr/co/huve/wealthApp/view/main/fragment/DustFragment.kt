package kr.co.huve.wealthApp.view.main.fragment

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
import kr.co.huve.wealthApp.R
import kr.co.huve.wealthApp.intent.WealthIntentFactory
import kr.co.huve.wealthApp.model.wealth.WealthModelStore
import kr.co.huve.wealthApp.model.wealth.WealthState
import kr.co.huve.wealthApp.util.WealthLocationManager
import kr.co.huve.wealthApp.view.EventObservable
import kr.co.huve.wealthApp.view.StateSubscriber
import kr.co.huve.wealthApp.view.main.DustView
import kr.co.huve.wealthApp.view.main.WealthViewEvent
import kr.co.huve.wealthApp.view.main.adapter.WealthPagerAdapter
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class DustFragment : Fragment(), StateSubscriber<WealthState>, EventObservable<WealthViewEvent> {
    @Inject
    lateinit var locationManager: WealthLocationManager

    @Inject
    lateinit var modelStore: WealthModelStore

    @Inject
    lateinit var intentFactory: WealthIntentFactory

    @Inject
    lateinit var dustView: DustView

    private val disposable = CompositeDisposable()
    private val requestRelay = PublishRelay.create<WealthViewEvent>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return dustView.view
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
                if (it.position == WealthPagerAdapter.Type.Dust.ordinal) requestDust()
            }
            is WealthState.DustDataReceived -> {
                dustView.bind(it.data)
                dustView.refreshProgress(false)
            }
            is WealthState.DustRequestError -> {
                Toast.makeText(this@DustFragment.context, it.message, Toast.LENGTH_SHORT).show()
                dustView.refreshProgress(false)
            }
            is WealthState.FailReceiveResponseFromAPI -> {
                Toast.makeText(
                    this@DustFragment.context,
                    getString(R.string.api_not_response),
                    Toast.LENGTH_SHORT
                ).show()
                dustView.refreshProgress(false)
            }
            is WealthState.DustRequestRunning -> {
                dustView.refreshProgress(true)
                dustView.requestStateChange(it.message)
                disposable.add(it.disposable)
            }
            else -> Unit
        }
    }

    override fun events(): Observable<WealthViewEvent> = requestRelay

    private fun requestDust() {
        if (!dustView.isbinded) {
            requestRelay.accept(
                WealthViewEvent.RequestDust(locationManager.getDetailCity())
            )
        }
    }
}