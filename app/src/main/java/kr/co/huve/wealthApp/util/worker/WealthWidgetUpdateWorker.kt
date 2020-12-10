package kr.co.huve.wealthApp.util.worker

import android.content.Context
import android.content.Intent
import android.location.Location
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.WorkerParameters
import com.google.gson.Gson
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import kr.co.huve.wealthApp.model.repository.data.*
import kr.co.huve.wealthApp.model.repository.data.dust.Dust
import kr.co.huve.wealthApp.model.repository.data.dust.RequestInfo
import kr.co.huve.wealthApp.model.repository.database.dao.PlaceDao
import kr.co.huve.wealthApp.model.repository.network.NetworkConfig
import kr.co.huve.wealthApp.model.repository.network.layer.CovidRestApi
import kr.co.huve.wealthApp.model.repository.network.layer.DustRestApi
import kr.co.huve.wealthApp.model.repository.network.layer.WeatherRestApi
import kr.co.huve.wealthApp.util.NotificationUtil
import kr.co.huve.wealthApp.util.WealthLocationManager
import kr.co.huve.wealthApp.view.widget.WealthWidget
import org.json.XML
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class WealthWidgetUpdateWorker @WorkerInject constructor(
    @Assisted val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    notificationUtil: NotificationUtil,
    private val locationManager: WealthLocationManager,
    private val weatherRestApi: WeatherRestApi,
    private val covidRestApi: CovidRestApi,
    private val dustRestApi: DustRestApi,
    private val placeDao: PlaceDao,
    private val gson: Gson
) : CommonRxWorker(appContext, workerParams, notificationUtil) {

    private val format = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

    override fun createWork(): Single<Result> {
        Timber.d("Widget worker created")
        setForegroundAsync(createForegroundInfo(NotificationRes.LocationForeground(context = appContext)))
        return locationManager.getLocation().concatMap { location ->
            val city = locationManager.getDetailCity()
            placeDao.loadNearPlaces(city)
                .subscribeOn(Schedulers.io())
                .concatMap {
                    Maybe.zip(
                        getWeatherRequest(location),
                        getCovidRequest(),
                        when (it.isEmpty()) {
                            true -> getDustRequest()
                            else -> getDustRequest(it.first().dustStation)
                        }
                    ) { weather: List<DayWeather>, covid: List<CovidItem>, dust: Dust ->
                        if (weather.isEmpty() || covid.isEmpty() || dust.items.isEmpty()) {
                            Result.retry()
                        } else {
                            appContext.sendBroadcast(Intent(appContext, WealthWidget::class.java)
                                .apply {
                                    putExtra(DataKey.EXTRA_WEATHER_DATA.name, weather.first())
                                    putExtra(
                                        DataKey.EXTRA_COVID_DATA.name,
                                        covid.first { covid -> covid.regionEng == "Total" }
                                    )
                                    putExtra(DataKey.EXTRA_DUST_DATA.name, dust)
                                    putExtra(DataKey.EXTRA_CITY_NAME.name, city)
                                    this.action = WealthWidget.RefreshAction
                                })
                            Result.success()
                        }
                    }
                }.onErrorReturn {
                    Timber.e("Error occur, retry again")
                    Result.retry()
                }
        }.toSingle()
    }

    private fun getWeatherRequest(lastLocation: Location): Maybe<List<DayWeather>> {
        return weatherRestApi.getTotalWeatherWithCoords(
            NetworkConfig.WEATHER_KEY,
            lastLocation.latitude,
            lastLocation.longitude,
            "minutely",
            "kr",
            "metric"
        ).map {
            listOf(it.current)
        }.onErrorReturn {
            Timber.e(it.toString())
            emptyList()
        }
    }

    private fun getCovidRequest(): Maybe<List<CovidItem>> {
        val calendar = Calendar.getInstance()
        val today = format.format(calendar.time)
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        val yesterday = format.format(calendar.time)
        return covidRestApi.getCovidStatus(
            key = NetworkConfig.COVID_KEY,
            page = 1,
            numOfRows = 40,
            startDate = yesterday,
            endDate = today
        ).map {
            gson.fromJson(
                XML.toJSONObject(it).toString(),
                CovidResult::class.java
            ).getItemList()
        }.onErrorReturn {
            Timber.e(it.toString())
            emptyList()
        }
    }

    private fun getDustRequest(stationName: String = "송파구"): Maybe<Dust> {
        return dustRestApi.getNearDustInfo(
            key = NetworkConfig.DUST_KEY,
            numOfRows = 1,
            page = 1,
            stationName = stationName,
            dataTerm = "DAILY",
            version = NetworkConfig.DUST_API_VERSION,
            returnType = "json"
        ).map { it }.onErrorReturn {
            Timber.e(it.toString())
            Dust(emptyList(), RequestInfo(stationName))
        }
    }
}