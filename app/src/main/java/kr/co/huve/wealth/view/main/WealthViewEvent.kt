package kr.co.huve.wealth.view.main

sealed class WealthViewEvent {
    data class PageChanged(val index: Int) : WealthViewEvent()
}