package kr.co.huve.wealth.view.splash

sealed class SplashViewEvent {
    object RequestWeatherFromActivity : SplashViewEvent()
}