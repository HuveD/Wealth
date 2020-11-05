package kr.co.huve.wealth.view.main

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_wealth.*
import kr.co.huve.wealth.R
import kr.co.huve.wealth.view.main.adapter.WealthPagerAdapter
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
}