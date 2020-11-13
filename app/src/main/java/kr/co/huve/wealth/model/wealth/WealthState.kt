package kr.co.huve.wealth.model.wealth

import io.reactivex.rxjava3.disposables.Disposable
import kr.co.huve.wealth.model.backend.data.CovidItem
import kr.co.huve.wealth.model.backend.data.dust.DustItem

sealed class WealthState {
    object IDLE : WealthState()
    object InvalidateStone : WealthState()
    data class FragmentSelected(val position: Int) : WealthState()
    data class WeatherTabChanged(val isHour: Boolean) : WealthState()

    data class RequestCovid(val disposable: Disposable) : WealthState()
    data class CovidDataReceived(val data: List<CovidItem>) : WealthState()
    object CovidRequestFail : WealthState()

    data class DustRequestRunning(val disposable: Disposable) : WealthState()
    data class DustDataReceived(val data: List<DustItem>) : WealthState()
    object DustRequestError : WealthState()
}