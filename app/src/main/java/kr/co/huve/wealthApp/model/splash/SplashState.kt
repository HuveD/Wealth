package kr.co.huve.wealthApp.model.splash

import kr.co.huve.wealthApp.model.repository.data.TotalWeather

sealed class SplashState {
    object Idle : SplashState()
    data class RequestPermission(val permission: String, var consumed: Boolean) : SplashState()
    data class Loading(val cancel: () -> Unit) : SplashState()
    data class Complete(val dataSet: TotalWeather) : SplashState()
    data class Error(val throwable: Throwable) : SplashState()
}