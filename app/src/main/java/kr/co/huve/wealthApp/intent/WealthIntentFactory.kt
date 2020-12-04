package kr.co.huve.wealthApp.intent

import android.content.Context
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped
import io.reactivex.rxjava3.schedulers.Schedulers
import kr.co.huve.wealthApp.R
import kr.co.huve.wealthApp.model.wealth.WealthModelStore
import kr.co.huve.wealthApp.model.wealth.WealthState
import kr.co.huve.wealthApp.util.WealthLocationManager
import kr.co.huve.wealthApp.model.repository.database.dao.PlaceDao
import kr.co.huve.wealthApp.model.repository.database.entity.Place
import kr.co.huve.wealthApp.model.repository.network.NetworkConfig
import kr.co.huve.wealthApp.model.repository.network.NetworkConfig.RETRY
import kr.co.huve.wealthApp.model.repository.data.CovidResult
import kr.co.huve.wealthApp.model.repository.data.dust.Dust
import kr.co.huve.wealthApp.model.repository.data.dust.DustStation
import kr.co.huve.wealthApp.model.repository.data.dust.DustStationItem
import kr.co.huve.wealthApp.model.repository.data.dust.TmCoord
import kr.co.huve.wealthApp.model.repository.network.layer.CovidRestApi
import kr.co.huve.wealthApp.model.repository.network.layer.DustRestApi
import kr.co.huve.wealthApp.model.repository.network.layer.KakaoRestApi
import kr.co.huve.wealthApp.view.main.WealthViewEvent
import org.json.XML
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject

@ActivityScoped
class WealthIntentFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val locationManager: WealthLocationManager,
    private val modelStore: WealthModelStore,
    private val covidRestApi: CovidRestApi,
    private val dustRestApi: DustRestApi,
    private val kakaoRestApi: KakaoRestApi,
    private val placeDao: PlaceDao,
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
            is WealthViewEvent.RequestDust -> buildRequestDustIntent()
            is WealthViewEvent.RefreshCovidDashboard -> intent { WealthState.RefreshCovidDashboard(viewEvent.item) }
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
                ).retry(RETRY).subscribeOn(Schedulers.io())
                    .subscribe(::retrofitSuccess, ::retrofitError)
            )
        }
    }

    private fun buildRequestDustIntent(): Intent<WealthState> = intent {
        WealthState.DustRequestRunning(
            placeDao.loadNearPlaces(locationManager.getDetailCity()).take(1)
                .subscribeOn(Schedulers.io())
                .subscribe({
                    if (it.isEmpty()) {
                        // request TM coordinates from wgs84
                        Timber.d("Request TM coordinates from wgs84")
                        buildRequestTmCoordIntent()
                    } else {
                        // reuse local data
                        Timber.d("Reuse local dust station data")
                        buildRequestDustInfoIntent(
                            DustStation(
                                stations = listOf(
                                    DustStationItem(
                                        0f,
                                        locationManager.getDetailCity(),
                                        it.first().dustStation
                                    )
                                )
                            )
                        )
                    }
                }, {
                    // request TM coordinates from wgs84
                    Timber.d("Request TM coordinates from wgs84")
                    buildRequestTmCoordIntent()
                }), context.getString(R.string.request_coordinates)
        )
    }

    private fun buildRequestTmCoordIntent() = chainedIntent {
        val location = locationManager.getLastLocation()
        WealthState.DustRequestRunning(
            kakaoRestApi.getTransverseMercatorCoordinate(
                auth = "KakaoAK ${NetworkConfig.KAKAO_REST_KEY}",
                lng = location.longitude,
                lat = location.latitude,
                outputCoordSystem = "TM"
            ).retry(RETRY).subscribeOn(Schedulers.io())
                .subscribe(::buildRequestDustStationIntent, ::retrofitError),
            context.getString(R.string.request_coordinates)
        )
    }

    private fun buildRequestDustStationIntent(tmCoord: TmCoord) = chainedIntent {
        // request dust station list
        if (tmCoord.items.isNotEmpty()) {
            val tmItem = tmCoord.items.first()
            fun retrofitSuccess(response: DustStation) {
                // Add to database
                if (response.stations.isNotEmpty()) {
                    val first = response.stations.first()
                    addStationInfoToDatabase(first)
                }
                buildRequestDustInfoIntent(response)
            }
            WealthState.DustRequestRunning(
                dustRestApi.getDustStation(
                    key = NetworkConfig.DUST_KEY,
                    numOfRows = 1,
                    page = 1,
                    tmX = tmItem.x,
                    tmY = tmItem.y,
                    returnType = "json"
                ).retry(RETRY).subscribeOn(Schedulers.io())
                    .subscribe(::retrofitSuccess, ::retrofitError),
                context.getString(R.string.find_dust_station)
            )
        } else WealthState.DustRequestError(context.getString(R.string.convert_tm_fail))
    }

    private fun buildRequestDustInfoIntent(response: DustStation) = chainedIntent {
        fun retrofitSuccess(response: Dust) = chainedIntent {
            if (response.items.isNotEmpty()) {
                WealthState.DustDataReceived(response.items.first())
            } else {
                WealthState.DustRequestError(context.getString(R.string.fail_find_dust_station))
            }
        }

        if (response.stations.isNotEmpty()) {
            // request dust info from selected station
            val station = response.stations.first()
            WealthState.DustRequestRunning(
                dustRestApi.getNearDustInfo(
                    key = NetworkConfig.DUST_KEY,
                    numOfRows = 1,
                    page = 1,
                    stationName = station.stationName,
                    dataTerm = "DAILY",
                    version = NetworkConfig.DUST_API_VERSION,
                    returnType = "json"
                ).retry(RETRY).subscribeOn(Schedulers.io())
                    .subscribe(::retrofitSuccess, ::retrofitError),
                context.getString(R.string.request_dust_info)
            )
        } else {
            WealthState.DustRequestError(context.getString(R.string.fail_receicve_dust_from_station))
        }
    }

    private fun addStationInfoToDatabase(item: DustStationItem) {
        Timber.d("Request Adding place.")
        val location = locationManager.getLastLocation()
        val place = Place(
            location.latitude,
            location.longitude,
            locationManager.getDetailCity(),
            item.stationName
        )
        placeDao.addPlace(place).subscribeOn(Schedulers.io()).onErrorReturn {
            Timber.d("Request update because of adding fail.")
            placeDao.updateConflictPlace(place).onErrorReturn {
                Timber.d("Update Error")
            }.subscribe()
        }.subscribe()
    }

    private fun retrofitError(t: Throwable) = chainedIntent {
        if (t is HttpException) Timber.d(
            t.response().toString()
        ) else Timber.d(t.toString())
        WealthState.FailReceiveResponseFromAPI
    }
}
