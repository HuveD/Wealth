package kr.co.huve.wealthApp.view.main

sealed class WealthViewEvent {
    data class PageChanged(val index: Int) : WealthViewEvent()
    data class WeatherTabChanged(val isHour: Boolean) : WealthViewEvent()
    data class RequestCovid(val dateString: String) : WealthViewEvent()
    data class RequestDust(val lat: Double, val lng: Double) : WealthViewEvent()
    object InvalidateStone : WealthViewEvent()
}