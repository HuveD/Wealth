package kr.co.huve.wealth.model

import kr.co.huve.wealth.model.backend.data.Weather

sealed class SplashState {
    object Idle : SplashState()
    data class Loading(val cancel: () -> Unit) : SplashState() {
        enum class Type {
            IDLE, PROGRESS_IN_WEATHER, COMPLETE
        }
    }

    data class Ready(val dataSet: List<Weather>) : SplashState()
    data class Error(val throwable: Throwable) : SplashState()
}