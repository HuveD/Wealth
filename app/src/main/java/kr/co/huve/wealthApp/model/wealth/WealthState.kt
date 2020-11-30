package kr.co.huve.wealthApp.model.wealth

import io.reactivex.rxjava3.disposables.Disposable
import kr.co.huve.wealthApp.util.repository.network.data.CovidItem
import kr.co.huve.wealthApp.util.repository.network.data.dust.DustItem

sealed class WealthState {
    object FailReceiveResponseFromAPI : WealthState()

    object IDLE : WealthState()
    object InvalidateStone : WealthState()
    data class FragmentSelected(val position: Int) : WealthState()
    data class WeatherTabChanged(val isHour: Boolean) : WealthState()

    data class RequestCovid(val disposable: Disposable) : WealthState()
    data class CovidDataReceived(val data: List<CovidItem>) : WealthState()
    data class RefreshCovidDashboard(val item: CovidItem) : WealthState()

    data class DustRequestRunning(val disposable: Disposable, val message: String) : WealthState()
    data class DustDataReceived(val data: DustItem) : WealthState()
    data class DustRequestError(val message: String) : WealthState()
}