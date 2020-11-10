package kr.co.huve.wealth.util.worker

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.WorkerParameters
import androidx.work.rxjava3.RxWorker
import androidx.work.workDataOf
import io.reactivex.rxjava3.core.Single
import kr.co.huve.wealth.R
import kr.co.huve.wealth.model.backend.NetworkConfig
import kr.co.huve.wealth.model.backend.data.TotalWeather
import kr.co.huve.wealth.model.backend.layer.WeatherRestApi
import kr.co.huve.wealth.util.WealthLocationManager
import kr.co.huve.wealth.util.data.DataKey

class WealthAlertCheckWorker @WorkerInject constructor(
    @Assisted val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    var locationManager: WealthLocationManager,
    var weatherApi: WeatherRestApi
) : RxWorker(appContext, workerParams) {

    override fun createWork(): Single<Result> {
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
            }
        )
    }

    private fun getDescription(totalWeather: TotalWeather): String {
        var sb = StringBuilder()
        if (totalWeather.daily.isNotEmpty()) {
            val today = totalWeather.daily.first()
            sb.append("오늘 온도는 ")
            sb.append(today.temp.day.toInt())
            sb.append(appContext.getString(R.string.symbol_celsius))
            sb.append("이며 체감 온도는 ")
            sb.append(today.feelsLike.day.toInt())
            sb.append(appContext.getString(R.string.symbol_celsius))
            sb.append("입니다. ")
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