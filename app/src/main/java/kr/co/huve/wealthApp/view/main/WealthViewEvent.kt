package kr.co.huve.wealthApp.view.main

import kr.co.huve.wealthApp.model.repository.data.CovidItem

sealed class WealthViewEvent {
    data class PageChanged(val index: Int) : WealthViewEvent()
    data class WeatherTabChanged(val isHour: Boolean) : WealthViewEvent()
    data class RequestCovid(val dateString: String) : WealthViewEvent()
    data class RefreshCovidDashboard(val item: CovidItem) : WealthViewEvent()
    object RequestDust : WealthViewEvent()
    object InvalidateStone : WealthViewEvent()
}