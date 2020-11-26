package kr.co.huve.wealthApp.view.main

sealed class WealthViewEvent {
    data class PageChanged(val index: Int) : WealthViewEvent()
    data class WeatherTabChanged(val isHour: Boolean) : WealthViewEvent()
    data class RequestCovid(val dateString: String) : WealthViewEvent()
    object RequestDust : WealthViewEvent()
    object InvalidateStone : WealthViewEvent()
}