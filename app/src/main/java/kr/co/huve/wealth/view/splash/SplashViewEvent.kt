package kr.co.huve.wealth.view.splash

sealed class SplashViewEvent {
    object CheckPermission : SplashViewEvent()
    object RequestWeatherFromActivity : SplashViewEvent()
}