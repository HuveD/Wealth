package kr.co.huve.wealth.intent

import com.google.gson.Gson
import dagger.hilt.android.scopes.ActivityScoped
import io.reactivex.rxjava3.schedulers.Schedulers
import kr.co.huve.wealth.model.backend.NetworkConfig
import kr.co.huve.wealth.model.backend.data.CovidResult
import kr.co.huve.wealth.model.backend.data.dust.Dust
import kr.co.huve.wealth.model.backend.data.dust.DustStation
import kr.co.huve.wealth.model.backend.data.dust.TmCoord
import kr.co.huve.wealth.model.backend.layer.CovidRestApi
import kr.co.huve.wealth.model.backend.layer.DustRestApi
import kr.co.huve.wealth.model.wealth.WealthModelStore
import kr.co.huve.wealth.model.wealth.WealthState
import kr.co.huve.wealth.util.WealthLocationManager
import kr.co.huve.wealth.view.main.WealthViewEvent
import org.json.XML
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject

@ActivityScoped
class WealthIntentFactory @Inject constructor(
    private val locationManager: WealthLocationManager,
    private val modelStore: WealthModelStore,
    private val covidRestApi: CovidRestApi,
    private val dustRestApi: DustRestApi,
    private val gson: Gson
) {
    fun process(event: WealthViewEvent) {
        modelStore.process(makeIntent(event))
    }

    private fun makeIntent(viewEvent: WealthViewEvent): Intent<WealthState> {
        return when (viewEvent) {
            is WealthViewEvent.PageChanged -> intent { WealthState.FragmentSelected(viewEvent.index) }
            is WealthViewEvent.WeatherTabChanged -> intent { WealthState.WeatherTabChanged(viewEvent.isHour) }
            is WealthViewEvent.InvalidateStone -> intent { WealthState.InvalidateStone }
            is WealthViewEvent.RequestCovid -> buildRequestCovidIntent(viewEvent.dateString)
            is WealthViewEvent.RequestDust -> buildRequestDustIntent(viewEvent.city)
        }
    }

    private fun chainedIntent(block: WealthState.() -> WealthState) =
        modelStore.process(intent(block))

    private fun buildRequestCovidIntent(dateString: String): Intent<WealthState> {
        return intent {
            fun retrofitSuccess(response: String) = chainedIntent {
                WealthState.CovidDataReceived(
                    data = gson.fromJson(
                        XML.toJSONObject(response).toString(),
                        CovidResult::class.java
                    ).getItemList()
                )
            }

            WealthState.RequestCovid(
                covidRestApi.getCovidStatus(
                    NetworkConfig.COVID_KEY,
                    1,
                    20,
                    dateString,
                    dateString
                ).subscribeOn(Schedulers.io()).subscribe(::retrofitSuccess, ::retrofitError)
            )
        }
    }

    private fun buildRequestDustIntent(city: String): Intent<WealthState> {
        return intent {
            WealthState.DustRequestRunning(
                dustRestApi.getTransverseMercatorCoordinate(
                    NetworkConfig.DUST_KEY,
                    city,
                    "json"
                ).subscribeOn(Schedulers.io())
                    .subscribe(::buildRequestDustStationSideEffect, ::retrofitError)
            )
        }
    }

    private fun buildRequestDustStationSideEffect(tmCoord: TmCoord) {
        return chainedIntent {
            if (tmCoord.items.isNotEmpty()) {
                val tmItem = tmCoord.items.first()
                WealthState.DustRequestRunning(
                    dustRestApi.getDustStation(
                        key = NetworkConfig.COVID_KEY,
                        numOfRows = 1,
                        page = 1,
                        tmX = tmItem.tmX,
                        tmY = tmItem.tmY,
                        returnType = "json"
                    ).subscribeOn(Schedulers.io())
                        .subscribe(::buildRequestDustStationIntent, ::retrofitError)
                )
            } else WealthState.DustRequestError
        }
    }

    private fun buildRequestDustStationIntent(response: DustStation) {
        return chainedIntent {

            fun retrofitSuccess(response: Dust) = chainedIntent {
                if (response.items.isNotEmpty()) {
                    WealthState.DustDataReceived(response.items)
                } else {
                    WealthState.DustRequestError
                }
            }

            if (response.stations.isNotEmpty()) {
                val station = response.stations.first()
                WealthState.DustRequestRunning(
                    dustRestApi.getNearDustInfo(
                        key = NetworkConfig.COVID_KEY,
                        numOfRows = 20,
                        page = 1,
                        stationName = station.stationName,
                        dataTerm = "DAILY",
                        version = "1.3",
                        returnType = "json"
                    ).subscribeOn(Schedulers.io()).subscribe(::retrofitSuccess, ::retrofitError)
                )
            } else {
                WealthState.DustRequestError
            }
        }
    }

    private fun retrofitError(t: Throwable) = chainedIntent {
        if (t is HttpException) Timber.d(
            t.response().toString()
        ) else Timber.d(t.toString())
        WealthState.DustRequestError
    }
}
