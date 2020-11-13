package kr.co.huve.wealth.intent

import android.content.Context
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped
import io.reactivex.rxjava3.schedulers.Schedulers
import kr.co.huve.wealth.R
import kr.co.huve.wealth.model.backend.NetworkConfig
import kr.co.huve.wealth.model.backend.data.CovidResult
import kr.co.huve.wealth.model.backend.data.dust.Dust
import kr.co.huve.wealth.model.backend.data.dust.DustStation
import kr.co.huve.wealth.model.backend.data.dust.TmCoord
import kr.co.huve.wealth.model.backend.layer.CovidRestApi
import kr.co.huve.wealth.model.backend.layer.DustRestApi
import kr.co.huve.wealth.model.wealth.WealthModelStore
import kr.co.huve.wealth.model.wealth.WealthState
import kr.co.huve.wealth.view.main.WealthViewEvent
import org.json.XML
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject

@ActivityScoped
class WealthIntentFactory @Inject constructor(
    @ApplicationContext private val context: Context,
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
                    key = NetworkConfig.COVID_KEY,
                    page = 1,
                    numOfRows = 20,
                    startDate = dateString,
                    endDate = dateString
                ).retry(3).subscribeOn(Schedulers.io())
                    .subscribe(::retrofitSuccess, ::retrofitError)
            )
        }
    }

    private fun buildRequestDustIntent(city: String): Intent<WealthState> {
        return intent {
            // request TM coordinates from wgs84
            WealthState.DustRequestRunning(
                dustRestApi.getTransverseMercatorCoordinate(
                    key = NetworkConfig.DUST_KEY,
                    numOfRows = 1,
                    page = 1,
                    city = city,
                    returnType = "json"
                ).retry(3).subscribeOn(Schedulers.io())
                    .subscribe(::buildRequestDustStationIntent, ::retrofitError)
            )
        }
    }

    private fun buildRequestDustStationIntent(tmCoord: TmCoord) {
        return chainedIntent {
            // request dust station list
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
                    ).retry(3).subscribeOn(Schedulers.io())
                        .subscribe(::buildRequestDustInfoIntent, ::retrofitError)
                )
            } else WealthState.DustRequestError(context.getString(R.string.convert_tm_fail))
        }
    }

    private fun buildRequestDustInfoIntent(response: DustStation) {
        return chainedIntent {
            // request dust info from selected station
            fun retrofitSuccess(response: Dust) = chainedIntent {
                if (response.items.isNotEmpty()) {
                    WealthState.DustDataReceived(response.items)
                } else {
                    WealthState.DustRequestError(context.getString(R.string.fail_find_dust_station))
                }
            }

            if (response.stations.isNotEmpty()) {
                val station = response.stations.first()
                WealthState.DustRequestRunning(
                    dustRestApi.getNearDustInfo(
                        key = NetworkConfig.COVID_KEY,
                        numOfRows = 1,
                        page = 1,
                        stationName = station.stationName,
                        dataTerm = "DAILY",
                        version = "1.3",
                        returnType = "json"
                    ).retry(3).subscribeOn(Schedulers.io())
                        .subscribe(::retrofitSuccess, ::retrofitError)
                )
            } else {
                WealthState.DustRequestError(context.getString(R.string.fail_receicve_dust_from_station))
            }
        }
    }

    private fun retrofitError(t: Throwable) = chainedIntent {
        if (t is HttpException) Timber.d(
            t.response().toString()
        ) else Timber.d(t.toString())
        WealthState.FailReceiveResponseFromAPI
    }
}
