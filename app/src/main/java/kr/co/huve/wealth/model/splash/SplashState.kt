package kr.co.huve.wealth.model.splash

import kr.co.huve.wealth.model.backend.data.TotalWeather

sealed class SplashState {
    object Idle : SplashState()
    data class RequestPermission(val permission: String, var consumed: Boolean) : SplashState()
    data class Loading(val cancel: () -> Unit) : SplashState()
    data class Complete(val dataSet: TotalWeather) : SplashState()
    data class Error(val throwable: Throwable) : SplashState()
}