package kr.co.huve.wealth.view.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jakewharton.rxrelay3.PublishRelay
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import kr.co.huve.wealth.intent.WealthIntentFactory
import kr.co.huve.wealth.model.wealth.WealthModelStore
import kr.co.huve.wealth.model.wealth.WealthState
import kr.co.huve.wealth.util.WealthLocationManager
import kr.co.huve.wealth.view.EventObservable
import kr.co.huve.wealth.view.StateSubscriber
import kr.co.huve.wealth.view.main.DustView
import kr.co.huve.wealth.view.main.WealthViewEvent
import kr.co.huve.wealth.view.main.adapter.WealthPagerAdapter
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
        dustView.bind(emptyList())
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
                if (it.position == WealthPagerAdapter.Companion.Type.Dust.ordinal) requestRelay.accept(
                    WealthViewEvent.RequestDust("삼전동")
                )
            }
            else -> Unit
        }
    }

    override fun events(): Observable<WealthViewEvent> = requestRelay
}