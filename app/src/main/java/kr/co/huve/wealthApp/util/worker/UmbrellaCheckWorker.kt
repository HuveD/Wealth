package kr.co.huve.wealthApp.util.worker

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import io.reactivex.rxjava3.core.Single
import kr.co.huve.wealthApp.R
import kr.co.huve.wealthApp.model.repository.data.DataKey
import kr.co.huve.wealthApp.model.repository.data.TotalWeather
import kr.co.huve.wealthApp.model.repository.network.NetworkConfig
import kr.co.huve.wealthApp.model.repository.network.layer.WeatherRestApi
import kr.co.huve.wealthApp.util.NotificationUtil
import kr.co.huve.wealthApp.util.WealthLocationManager
import kr.co.huve.wealthApp.view.widget.WidgetUpdateService
import kotlin.math.roundToInt

class UmbrellaCheckWorker @WorkerInject constructor(
    @Assisted val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    notificationUtil: NotificationUtil,
    var locationManager: WealthLocationManager,
    var weatherApi: WeatherRestApi
) : CommonRxWorker(appContext, workerParams, notificationUtil) {

    override fun createWork(): Single<Result> {
        // Because of Re-initial foreground service bug on WorkManager lib, Do it directly as a temporary.
//        setForegroundAsync(createForegroundInfo(NotificationRes.LocationForeground(context = appContext)))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Don't need to foreground under the api 26. This foreground service is used to access user location.
            appContext.startForegroundService(Intent(appContext, WidgetUpdateService::class.java))
        }
        val lastLocation = locationManager.getLastLocation()
        return weatherApi.getTotalWeatherWithCoords(
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
        }.doFinally {
            stopForeground(Intent(appContext, WidgetUpdateService::class.java))
        }.toSingle()
    }

    override fun onStopped() {
        super.onStopped()
        stopForeground(Intent(appContext, WidgetUpdateService::class.java))
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