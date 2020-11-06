package kr.co.huve.wealth.model.wealth

sealed class WealthState {
    object IDLE : WealthState()
    object InvalidateStone : WealthState()
    data class FragmentSelected(val position: Int) : WealthState()
    data class WeatherTabChanged(val isHour: Boolean) : WealthState()
}