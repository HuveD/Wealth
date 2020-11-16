package kr.co.huve.wealthApp.intent

import dagger.hilt.android.scopes.ActivityScoped
import io.reactivex.rxjava3.schedulers.Schedulers
import kr.co.huve.wealthApp.model.backend.NetworkConfig
import kr.co.huve.wealthApp.model.backend.NetworkConfig.RETRY
import kr.co.huve.wealthApp.model.backend.data.TotalWeather
import kr.co.huve.wealthApp.model.backend.layer.CovidRestApi
import kr.co.huve.wealthApp.model.backend.layer.WeatherRestApi
import kr.co.huve.wealthApp.model.splash.SplashModelStore
import kr.co.huve.wealthApp.model.splash.SplashState
import kr.co.huve.wealthApp.util.WealthLocationManager
import kr.co.huve.wealthApp.view.splash.SplashViewEvent
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject

@ActivityScoped
class SplashIntentFactory @Inject constructor(
    private val splashModelStore: SplashModelStore,
    private val weatherRestApi: WeatherRestApi,
    private val covidRestApi: CovidRestApi,
    private val locationManager: WealthLocationManager
) {

    fun process(event: SplashViewEvent) {
        splashModelStore.process(makeIntent(event))
    }

    private fun makeIntent(viewEvent: SplashViewEvent): Intent<SplashState> {
        return when (viewEvent) {
            is SplashViewEvent.CheckPermission -> buildCheckPermissionIntent(viewEvent.permission)
            is SplashViewEvent.PermissionGranted -> buildWeatherRequestIntent()
        }
    }

    private fun chainedIntent(block: SplashState.() -> SplashState) =
        splashModelStore.process(intent(block))

    private fun buildCheckPermissionIntent(it: String): Intent<SplashState> {
        return intent {
            SplashState.RequestPermission(permission = it, consumed = false)
        }
    }

    private fun buildWeatherRequestIntent(): Intent<SplashState> {
        return intent {
            fun retrofitSuccess(data: TotalWeather) = chainedIntent {
                SplashState.Complete(dataSet = data)
            }

            fun retrofitError(t: Throwable) = chainedIntent {
                if (t is HttpException) Timber.d(
                    t.response().toString()
                ) else Timber.d(t.toString())
                SplashState.Error(throwable = t)
            }

            // 현재 위치에 날씨 요청
            val lastLocation = locationManager.getLastLocation()
            val disposable =
                weatherRestApi.getTotalWeatherWithCoords(
                    NetworkConfig.WEATHER_KEY,
                    lastLocation.latitude,
                    lastLocation.longitude,
                    "minutely",
                    "kr",
                    "metric"
                ).retry(RETRY).subscribeOn(Schedulers.io())
                    .subscribe(::retrofitSuccess, ::retrofitError)
            SplashState.Loading(disposable::dispose)
        }
    }
}