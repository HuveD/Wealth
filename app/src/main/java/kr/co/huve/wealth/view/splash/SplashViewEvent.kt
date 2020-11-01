package kr.co.huve.wealth.view.splash

sealed class SplashViewEvent {
    data class CheckPermission(val permission: String) : SplashViewEvent()
    object PermissionGranted : SplashViewEvent()
}