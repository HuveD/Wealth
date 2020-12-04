package kr.co.huve.wealthApp.util.worker

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import io.reactivex.rxjava3.core.Single
import kr.co.huve.wealthApp.R
import kr.co.huve.wealthApp.util.NotificationUtil
import kr.co.huve.wealthApp.util.WealthLocationManager
import kr.co.huve.wealthApp.model.repository.data.DataKey
import kr.co.huve.wealthApp.model.repository.data.NotificationRes
import kr.co.huve.wealthApp.model.repository.network.NetworkConfig
import kr.co.huve.wealthApp.model.repository.data.TotalWeather
import kr.co.huve.wealthApp.model.repository.network.layer.WeatherRestApi
import kotlin.math.roundToInt

class UmbrellaCheckWorker @WorkerInject constructor(
    @Assisted val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    notificationUtil: NotificationUtil,
    var locationManager: WealthLocationManager,
    var weatherApi: WeatherRestApi
) : CommonRxWorker(appContext, workerParams, notificationUtil) {

    override fun createWork(): Single<Result> {
        setForegroundAsync(createForegroundInfo(NotificationRes.LocationForeground(context = appContext)))
        val lastLocation = locationManager.getLastLocation()
        return Single.fromObservable(
            weatherApi.getTotalWeatherWithCoords(
                NetworkConfig.WEATHER_KEY,
                lastLocation.latitude,
                lastLocation.longitude,
                "minutely",
                "kr",
                "metric"
            ).map {
                val outputData = workDataOf(
                    DataKey.WORK_NEED_UMBRELLA.name to needUmbrella(it),
                    DataKey.WORK_WEATHER_DESCRIPTION.name to getDescription(it)
                )
                Result.success(outputData)
            }.onErrorReturn {
                Result.retry()
            }
        )
    }

    private fun getDescription(totalWeather: TotalWeather): String {
        val sb = StringBuilder()
        if (totalWeather.daily.isNotEmpty()) {
            val format = appContext.getString(R.string.daily_alert_format)
            val today = totalWeather.daily.first()
            sb.append(
                String.format(
                    format,
                    today.temp.day.roundToInt(),
                    today.feelsLike.day.roundToInt()
                )
            )
        }
        return sb.toString()
    }

    private fun needUmbrella(totalWeather: TotalWeather): Boolean {
        var need = false
        for (data in totalWeather.hourly.subList(0, 12)) {
            if (data.rain != null || data.snow != null) {
                need = true
                break
            }
        }
        return need
    }
}