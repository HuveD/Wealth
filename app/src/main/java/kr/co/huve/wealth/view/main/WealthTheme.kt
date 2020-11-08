package kr.co.huve.wealth.view.main

import android.content.Context
import androidx.core.content.ContextCompat
import kr.co.huve.wealth.R

sealed class WealthTheme {
    object WeatherSunset : WealthTheme()
    object WeatherNight : WealthTheme()
    object WeatherDaytime : WealthTheme()
    object CovidSafe : WealthTheme()
    object CovidNormal : WealthTheme()
    object CovidDanger : WealthTheme()

    fun getBackgroundColor(context: Context) = when (this) {
        CovidSafe -> ContextCompat.getColor(context, R.color.iconic_safe)
        CovidNormal -> ContextCompat.getColor(context, R.color.iconic_little_warn)
        CovidDanger -> ContextCompat.getColor(context, R.color.iconic_warn)
        WeatherSunset -> ContextCompat.getColor(context, R.color.iconic_sunset)
        WeatherNight -> ContextCompat.getColor(context, R.color.iconic_dark_blue)
        WeatherDaytime -> ContextCompat.getColor(context, R.color.iconic_sky_blue)
        else -> 0
    }

    fun getFigureColor(context: Context) = when (this) {
        CovidSafe -> ContextCompat.getColor(context, R.color.iconic_white)
        CovidDanger, CovidNormal -> ContextCompat.getColor(context, R.color.iconic_red)
        else -> 0
    }

    fun getLabelColor(context: Context) = when (this) {
        CovidSafe -> ContextCompat.getColor(context, R.color.iconic_white)
        CovidDanger, CovidNormal -> ContextCompat.getColor(context, R.color.alpha_black)
        else -> 0
    }
}