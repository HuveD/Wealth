package kr.co.huve.wealth.model

import kr.co.huve.wealth.model.backend.data.Weather

sealed class SplashState {
    object Idle : SplashState()
    data class Loading(val cancel: () -> Unit) : SplashState()
    data class Ready(val dataSet: Weather) : SplashState()
    data class Error(val throwable: Throwable) : SplashState()
}