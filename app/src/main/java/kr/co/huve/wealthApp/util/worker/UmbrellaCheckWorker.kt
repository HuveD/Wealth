package kr.co.huve.wealthApp.util.worker

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.WorkerParameters
import androidx.work.rxjava3.RxWorker
import androidx.work.workDataOf
import io.reactivex.rxjava3.core.Single
import kr.co.huve.wealthApp.R
import kr.co.huve.wealthApp.model.backend.NetworkConfig
import kr.co.huve.wealthApp.model.backend.NetworkConfig.RETRY
import kr.co.huve.wealthApp.model.backend.data.TotalWeather
import kr.co.huve.wealthApp.model.backend.layer.WeatherRestApi
import kr.co.huve.wealthApp.util.WealthLocationManager
import kr.co.huve.wealthApp.util.data.DataKey
import timber.log.Timber

class UmbrellaCheckWorker @WorkerInject constructor(
    @Assisted val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    var locationManager: WealthLocationManager,
    var weatherApi: WeatherRestApi
) : RxWorker(appContext, workerParams) {

    override fun createWork(): Single<Result> {
        Timber.d("WealthAlertCheckWorker Created")
        val lastLocation = locationManager.getLastLocation()
        return Single.fromObservable(
            weatherApi.getTotalWeatherWithCoords(
                NetworkConfig.WEATHER_KEY,
                lastLocation.latitude,
                lastLocation.longitude,
                "minutely",
                "kr",
                "metric"
            ).retry(RETRY).map {
                val outputData = workDataOf(
                    DataKey.WORK_NEED_UMBRELLA.name to needUmbrella(it),
                    DataKey.WORK_WEATHER_DESCRIPTION.name to getDescription(it)
                )
                Timber.d("WealthAlertCheckWorker Success")
                Result.success(outputData)
            }
        )
    }

    private fun getDescription(totalWeather: TotalWeather): String {
        val sb = StringBuilder()
        if (totalWeather.daily.isNotEmpty()) {
            val format = appContext.getString(R.string.daily_alert_format)
            val today = totalWeather.daily.first()
            sb.append(String.format(format, today.temp.day.toInt(), today.feelsLike.day.toInt()))
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