package kr.co.huve.wealthApp.view.main

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.jakewharton.rxrelay3.PublishRelay
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.activity_wealth.*
import kr.co.huve.wealthApp.R
import kr.co.huve.wealthApp.intent.WealthIntentFactory
import kr.co.huve.wealthApp.model.wealth.WealthModelStore
import kr.co.huve.wealthApp.model.wealth.WealthState
import kr.co.huve.wealthApp.util.DepthPageTransformer
import kr.co.huve.wealthApp.view.EventObservable
import kr.co.huve.wealthApp.view.StateSubscriber
import kr.co.huve.wealthApp.view.main.adapter.WealthPagerAdapter
import javax.inject.Inject

@AndroidEntryPoint
class WealthActivity : FragmentActivity(), StateSubscriber<WealthState>,
    EventObservable<WealthViewEvent> {

    @Inject
    lateinit var viewPagerAdapter: WealthPagerAdapter

    @Inject
    lateinit var intentFactory: WealthIntentFactory

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
            when (it) {
                WealthState.IDLE -> initializePager()
                is WealthState.FragmentSelected -> invalidateIndicator(it.position)
                else -> Unit
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
        viewPager.setPageTransformer(DepthPageTransformer())
        viewPager.adapter = viewPagerAdapter
    }

    private fun invalidateIndicator(position: Int) {
        pageIndicator1.isActivated = position == 0
        pageIndicator2.isActivated = position == 1
        pageIndicator3.isActivated = position == 2
    }
}