package kr.co.huve.wealth.view.main

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.jakewharton.rxrelay3.PublishRelay
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.activity_wealth.*
import kr.co.huve.wealth.R
import kr.co.huve.wealth.intent.WeatherIntentFactory
import kr.co.huve.wealth.model.wealth.WealthModelStore
import kr.co.huve.wealth.model.wealth.WealthState
import kr.co.huve.wealth.view.EventObservable
import kr.co.huve.wealth.view.StateSubscriber
import kr.co.huve.wealth.view.main.adapter.WealthPagerAdapter
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class WealthActivity : FragmentActivity(), StateSubscriber<WealthState>,
    EventObservable<WealthViewEvent> {

    @Inject
    lateinit var viewPagerAdapter: WealthPagerAdapter

    @Inject
    lateinit var intentFactory: WeatherIntentFactory

    @Inject
    lateinit var modelStore: WealthModelStore

    private val pageSelectedRelay = PublishRelay.create<WealthViewEvent>()
    private val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wealth)
    }

    override fun onBackPressed() {
        if (viewPager.currentItem == 0) {
            super.onBackPressed()
        } else {
            viewPager.currentItem = viewPager.currentItem - 1
        }
    }

    override fun onResume() {
        super.onResume()
        viewPager.registerOnPageChangeCallback(pageChanged)
        disposable.add(events().subscribe(intentFactory::process))
        disposable.add(modelStore.modelState().subscribeToState())
    }

    override fun onPause() {
        super.onPause()
        disposable.clear()
        viewPager.unregisterOnPageChangeCallback(pageChanged)
    }

    override fun Observable<WealthState>.subscribeToState(): Disposable {
        return subscribe {
            Timber.d("State Changed: $it")
            when (it) {
                WealthState.IDLE -> initializePager()
                is WealthState.FragmentSelected -> invalidateIndicator(it.position)
            }
        }
    }

    override fun events(): Observable<WealthViewEvent> {
        return pageSelectedRelay
    }

    private val pageChanged = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            pageSelectedRelay.accept(WealthViewEvent.PageChanged(position))
        }
    }

    private fun initializePager() {
        window.statusBarColor = ContextCompat.getColor(this, R.color.iconic_dark_blue)
        viewPager.adapter = viewPagerAdapter
    }

    private fun invalidateIndicator(position: Int) {
        pageIndicator1.isActivated = position == 0
        pageIndicator2.isActivated = position == 1
        pageIndicator3.isActivated = position == 2
    }
}