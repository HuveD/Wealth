package kr.co.huve.wealth.view.main.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import kr.co.huve.wealth.view.main.fragment.WeatherFragment
import javax.inject.Inject

private const val PAGE_NUM = 1

class WealthPagerAdapter @Inject constructor(activity: FragmentActivity) :
    FragmentStateAdapter(activity) {

    override fun getItemCount(): Int {
        return PAGE_NUM;
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> WeatherFragment()
            else -> WeatherFragment()
        }
    }
}