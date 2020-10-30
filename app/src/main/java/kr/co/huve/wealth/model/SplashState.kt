package kr.co.huve.wealth.model

sealed class SplashState {
    object Bored(var nm : Int) : SplashState() {
        fun loadWeather() = Loading
    }

    object Loading : SplashState()
}