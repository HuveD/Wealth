package kr.co.huve.wealth.util

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.WorkerParameters
import androidx.work.rxjava3.RxWorker
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import kr.co.huve.wealth.model.backend.NetworkConfig
import kr.co.huve.wealth.model.backend.layer.WeatherRestApi
import timber.log.Timber

class ShowWealthAlertWorker @WorkerInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    var locationManager: WealthLocationManager,
    var weatherApi: WeatherRestApi
) :
    RxWorker(appContext, workerParams) {

    override fun createWork(): Single<Result> {
        val lastLocation = locationManager.getLastLocation()
        return Observable.range(0, 1)
            .flatMap {
                weatherApi.getTotalWeatherWithCoords(
                    NetworkConfig.WEATHER_KEY,
                    lastLocation.latitude,
                    lastLocation.longitude,
                    "minutely",
                    "kr",
                    "metric"
                )
            }
            .map { result ->
                Timber.d("$result")
            }
            .toList()
            .map {
                Timber.d("완료")
                Result.success()
            }
    }
}