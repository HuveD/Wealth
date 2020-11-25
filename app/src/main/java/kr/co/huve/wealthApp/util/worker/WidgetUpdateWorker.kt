package kr.co.huve.wealthApp.util.worker

import android.content.Context
import android.content.Intent
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.WorkerParameters
import com.google.gson.Gson
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import kr.co.huve.wealthApp.model.backend.NetworkConfig
import kr.co.huve.wealthApp.model.backend.data.CovidItem
import kr.co.huve.wealthApp.model.backend.data.CovidResult
import kr.co.huve.wealthApp.model.backend.data.DayWeather
import kr.co.huve.wealthApp.model.backend.data.dust.DustItem
import kr.co.huve.wealthApp.model.backend.layer.CovidRestApi
import kr.co.huve.wealthApp.model.backend.layer.DustRestApi
import kr.co.huve.wealthApp.model.backend.layer.WeatherRestApi
import kr.co.huve.wealthApp.util.NotificationUtil
import kr.co.huve.wealthApp.util.WealthLocationManager
import kr.co.huve.wealthApp.util.data.DataKey
import kr.co.huve.wealthApp.util.data.NotificationRes
import kr.co.huve.wealthApp.view.widget.WealthWidget
import org.json.XML

class WidgetUpdateWorker @WorkerInject constructor(
    @Assisted val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    notificationUtil: NotificationUtil,
    private val locationManager: WealthLocationManager,
    private val weatherRestApi: WeatherRestApi,
    private val covidRestApi: CovidRestApi,
    private val dustRestApi: DustRestApi,
    private val gson: Gson
) : CommonRxWorker(appContext, workerParams, notificationUtil) {

    override fun createWork(): Single<Result> {
        setForegroundAsync(createForegroundInfo(NotificationRes.LocationForeground(context = appContext)))
        val source = Observable.zip(
            getWeatherRequest(),
            getCovidRequest(),
            getDustRequest()
        ) { weather: List<DayWeather>, covid: List<CovidItem>, dust: List<DustItem> ->
            if (weather.isEmpty() || covid.isEmpty() || dust.isEmpty()) {
                Result.retry()
            } else {
                appContext.sendBroadcast(Intent(appContext, WealthWidget::class.java)
                    .apply {
                        putExtra(DataKey.EXTRA_WEATHER_DATA.name, weather.first())
                        putExtra(DataKey.EXTRA_COVID_DATA.name, covid.reversed().first())
                        putExtra(DataKey.EXTRA_DUST_DATA.name, dust.first())
                        this.action = WealthWidget.InvalidateAction
                    })
                Result.success()
            }
        }
        return Single.fromObservable(source)
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
            emptyList()
        }
    }

    private fun getCovidRequest(): Observable<List<CovidItem>> {
        return covidRestApi.getCovidStatus(
            key = NetworkConfig.COVID_KEY,
            page = 1,
            numOfRows = 20,
            startDate = "20201124",
            endDate = "20201124"
        ).map {
            gson.fromJson(
                XML.toJSONObject(it).toString(),
                CovidResult::class.java
            ).getItemList()
        }.onErrorReturn {
            emptyList()
        }
    }

    private fun getDustRequest(): Observable<List<DustItem>> {
        return dustRestApi.getNearDustInfo(
            key = NetworkConfig.DUST_KEY,
            numOfRows = 1,
            page = 1,
            stationName = "송파구",
            dataTerm = "DAILY",
            version = NetworkConfig.DUST_API_VERSION,
            returnType = "json"
        ).map {
            it.items
        }.onErrorReturn {
            emptyList()
        }
    }
}