package kr.co.huve.wealth.model.wealth

import io.reactivex.rxjava3.disposables.Disposable
import kr.co.huve.wealth.model.backend.data.Item

sealed class WealthState {
    object IDLE : WealthState()
    object InvalidateStone : WealthState()
    data class FragmentSelected(val position: Int) : WealthState()
    data class WeatherTabChanged(val isHour: Boolean) : WealthState()

    data class RequestCovid(val disposable: Disposable) : WealthState()
    data class CovidDataReceived(val data: List<Item>) : WealthState()
    object CovidRequestFail : WealthState()

    data class DustRequestRunning(val disposable: Disposable) : WealthState()
    object DustRequestFinish : WealthState()
    object DustRequestError : WealthState()
}