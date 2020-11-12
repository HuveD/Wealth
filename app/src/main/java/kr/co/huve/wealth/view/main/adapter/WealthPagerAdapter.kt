package kr.co.huve.wealth.view.main.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import kr.co.huve.wealth.view.main.fragment.CovidFragment
import kr.co.huve.wealth.view.main.fragment.DustFragment
import kr.co.huve.wealth.view.main.fragment.WeatherFragment
import javax.inject.Inject

private const val PAGE_NUM = 3

class WealthPagerAdapter @Inject constructor(activity: FragmentActivity) :
    FragmentStateAdapter(activity) {

    override fun getItemCount(): Int {
        return PAGE_NUM;
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            Type.Weather.ordinal -> WeatherFragment()
            Type.Covid.ordinal -> CovidFragment()
            else -> DustFragment()
        }
    }

    companion object {
        enum class Type {
            Weather,
            Covid,
            Dust
        }
    }
}