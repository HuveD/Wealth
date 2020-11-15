package kr.co.huve.wealthApp.view.splash

sealed class SplashViewEvent {
    data class CheckPermission(val permission: String) : SplashViewEvent()
    object PermissionGranted : SplashViewEvent()
}