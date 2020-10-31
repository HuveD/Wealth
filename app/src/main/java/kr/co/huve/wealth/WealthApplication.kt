package kr.co.huve.wealth

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WealthApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}