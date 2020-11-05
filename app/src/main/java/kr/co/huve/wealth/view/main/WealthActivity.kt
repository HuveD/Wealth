package kr.co.huve.wealth.view.main

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_wealth.*
import kr.co.huve.wealth.R
import kr.co.huve.wealth.view.main.adapter.WealthPagerAdapter
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class WealthActivity : FragmentActivity() {

    @Inject
    lateinit var viewPagerAdapter: WealthPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = ContextCompat.getColor(this, R.color.iconic_dark_blue)
        setContentView(R.layout.activity_wealth)
        viewPager.adapter = viewPagerAdapter
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
    }

    override fun onPause() {
        super.onPause()
        viewPager.unregisterOnPageChangeCallback(pageChanged)
    }

    private val pageChanged = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            Timber.d("PageSelected $position")
            when (position) {
                0 -> {
                    pageIndicator1.isActivated = true
                    pageIndicator2.isActivated = false
                    pageIndicator3.isActivated = false
                }
                1 -> {
                    pageIndicator1.isActivated = false
                    pageIndicator2.isActivated = true
                    pageIndicator3.isActivated = false
                }
                else -> {
                    pageIndicator1.isActivated = false
                    pageIndicator2.isActivated = false
                    pageIndicator3.isActivated = true
                }
            }
        }
    }
}