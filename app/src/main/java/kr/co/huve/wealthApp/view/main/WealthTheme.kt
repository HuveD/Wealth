package kr.co.huve.wealthApp.view.main

import android.content.Context
import androidx.core.content.ContextCompat
import kr.co.huve.wealthApp.R

sealed class WealthTheme {
    object WeatherSunset : WealthTheme()
    object WeatherSunrise : WealthTheme()
    object WeatherNight : WealthTheme()
    object WeatherDaytime : WealthTheme()
    object CovidSafe : WealthTheme()
    object CovidNormal : WealthTheme()
    object CovidDanger : WealthTheme()
    object DustGood : WealthTheme()
    object DustNormal : WealthTheme()
    object DustBad : WealthTheme()
    object DustTooBad : WealthTheme()

    fun getBackgroundColor(context: Context) = when (this) {
        CovidSafe -> ContextCompat.getColor(context, R.color.iconic_safe)
        CovidNormal -> ContextCompat.getColor(context, R.color.iconic_little_warn)
        CovidDanger -> ContextCompat.getColor(context, R.color.iconic_warn)
        else -> 0
    }

    fun getBackgroundResource() = when (this) {
        WeatherSunset -> R.drawable.bg_sunset
        WeatherSunrise -> R.drawable.bg_sunrise
        WeatherNight -> R.drawable.bg_night
        WeatherDaytime, CovidSafe, DustNormal -> R.drawable.bg_daytime
        CovidNormal, DustBad -> R.drawable.bg_covid_normal
        CovidDanger, DustTooBad -> R.drawable.bg_covid_warn
        DustGood -> R.drawable.bg_good
    }

    fun getFigureColor(context: Context) = when (this) {
        CovidSafe -> ContextCompat.getColor(context, R.color.iconic_white)
//        CovidDanger, CovidNormal -> ContextCompat.getColor(context, R.color.iconic_red)
        else -> ContextCompat.getColor(context, R.color.iconic_white)
    }

    fun getLabelColor(context: Context) = when (this) {
        CovidSafe -> ContextCompat.getColor(context, R.color.iconic_white)
//        CovidDanger, CovidNormal -> ContextCompat.getColor(context, R.color.alpha_black)
        else -> ContextCompat.getColor(context, R.color.iconic_white)
    }
}