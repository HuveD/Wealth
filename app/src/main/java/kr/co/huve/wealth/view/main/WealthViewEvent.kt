package kr.co.huve.wealth.view.main

sealed class WealthViewEvent {
    data class PageChanged(val index: Int) : WealthViewEvent()
    data class WeatherTabChanged(val isHour: Boolean) : WealthViewEvent()
    object InvalidateStone : WealthViewEvent()
}