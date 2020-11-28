package kr.co.huve.wealthApp.util.worker

import android.content.Context
import android.content.Intent
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.WorkerParameters
import com.google.gson.Gson
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import kr.co.huve.wealthApp.util.NotificationUtil
import kr.co.huve.wealthApp.util.WealthLocationManager
import kr.co.huve.wealthApp.util.data.DataKey
import kr.co.huve.wealthApp.util.data.NotificationRes
import kr.co.huve.wealthApp.util.repository.database.dao.PlaceDao
import kr.co.huve.wealthApp.util.repository.network.NetworkConfig
import kr.co.huve.wealthApp.util.repository.network.data.CovidItem
import kr.co.huve.wealthApp.util.repository.network.data.CovidResult
import kr.co.huve.wealthApp.util.repository.network.data.DayWeather
import kr.co.huve.wealthApp.util.repository.network.data.dust.Dust
import kr.co.huve.wealthApp.util.repository.network.data.dust.RequestInfo
import kr.co.huve.wealthApp.util.repository.network.layer.CovidRestApi
import kr.co.huve.wealthApp.util.repository.network.layer.DustRestApi
import kr.co.huve.wealthApp.util.repository.network.layer.WeatherRestApi
import kr.co.huve.wealthApp.view.widget.WealthWidget
import org.json.XML
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class WidgetUpdateWorker @WorkerInject constructor(
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
        setForegroundAsync(createForegroundInfo(NotificationRes.LocationForeground(context = appContext)))
        val city = locationManager.getDetailCity()
        return Single.fromObservable(
            placeDao.loadNearPlaces(city).concatMap {
                Observable.zip(
                    getWeatherRequest(),
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
                                this.action = WealthWidget.InvalidateAction
                            })
                        Result.success()
                    }
                }
            }.take(1)
        )
    }

    private fun getWeatherRequest(): Observable<List<DayWeather>> {
        val lastLocation = locationManager.getLastLocation()
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
            Timber.d(it.toString())
            emptyList()
        }
    }

    private fun getCovidRequest(): Observable<List<CovidItem>> {
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
            Timber.d(it.toString())
            emptyList()
        }
    }

    private fun getDustRequest(stationName: String = "송파구"): Observable<Dust> {
        return dustRestApi.getNearDustInfo(
            key = NetworkConfig.DUST_KEY,
            numOfRows = 1,
            page = 1,
            stationName = stationName,
            dataTerm = "DAILY",
            version = NetworkConfig.DUST_API_VERSION,
            returnType = "json"
        ).map { it }.onErrorReturn {
            Timber.d(it.toString())
            Dust(emptyList(), RequestInfo(stationName))
        }
    }
}