package kr.co.huve.wealthApp.view.splash

import android.location.Location

sealed class SplashViewEvent {
    data class CheckPermission(val permission: String) : SplashViewEvent()
    data class PermissionGranted(val location: Location) : SplashViewEvent()
}