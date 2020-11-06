package kr.co.huve.wealth.model.wealth

sealed class WealthState {
    object IDLE : WealthState()
    data class FragmentSelected(val position: Int) : WealthState()
}