package kr.co.huve.wealth.view.main

import android.content.Context
import androidx.core.content.ContextCompat
import kr.co.huve.wealth.R

sealed class CovidTheme {
    object Safe : CovidTheme()
    object Normal : CovidTheme()
    object Danger : CovidTheme()

    fun getBackgroundColor(context: Context) = when (this) {
        is Safe -> ContextCompat.getColor(context, R.color.iconic_safe)
        is Normal -> ContextCompat.getColor(context, R.color.iconic_little_warn)
        is Danger -> ContextCompat.getColor(context, R.color.iconic_warn)
    }

    fun getFigureColor(context: Context) = when (this) {
        is Safe -> ContextCompat.getColor(context, R.color.iconic_white)
        else -> ContextCompat.getColor(context, R.color.iconic_red)
    }

    fun getLabelColor(context: Context) = when (this) {
        is Safe -> ContextCompat.getColor(context, R.color.iconic_white)
        else -> ContextCompat.getColor(context, R.color.alpha_black)
    }
}