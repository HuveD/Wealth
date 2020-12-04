package kr.co.huve.wealthApp.intent

import android.location.Location
import dagger.hilt.android.scopes.ActivityScoped
import io.reactivex.rxjava3.schedulers.Schedulers
import kr.co.huve.wealthApp.model.splash.SplashModelStore
import kr.co.huve.wealthApp.model.splash.SplashState
import kr.co.huve.wealthApp.model.repository.network.NetworkConfig
import kr.co.huve.wealthApp.model.repository.network.NetworkConfig.RETRY
import kr.co.huve.wealthApp.model.repository.data.TotalWeather
import kr.co.huve.wealthApp.model.repository.network.layer.WeatherRestApi
import kr.co.huve.wealthApp.view.splash.SplashViewEvent
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject

@ActivityScoped
class SplashIntentFactory @Inject constructor(
    private val splashModelStore: SplashModelStore,
    private val weatherRestApi: WeatherRestApi
) {

    fun process(event: SplashViewEvent) {
        splashModelStore.process(makeIntent(event))
    }

    private fun makeIntent(viewEvent: SplashViewEvent): Intent<SplashState> {
        return when (viewEvent) {
            is SplashViewEvent.CheckPermission -> buildCheckPermissionIntent(viewEvent.permission)
            is SplashViewEvent.PermissionGranted -> buildWeatherRequestIntent(viewEvent.location)
        }
    }

    private fun chainedIntent(block: SplashState.() -> SplashState) =
        splashModelStore.process(intent(block))

    private fun buildCheckPermissionIntent(it: String): Intent<SplashState> {
        return intent {
            SplashState.RequestPermission(permission = it, consumed = false)
        }
    }

    private fun buildWeatherRequestIntent(location: Location): Intent<SplashState> {
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
            val disposable =
                weatherRestApi.getTotalWeatherWithCoords(
                    NetworkConfig.WEATHER_KEY,
                    location.latitude,
                    location.longitude,
                    "minutely",
                    "kr",
                    "metric"
                ).retry(RETRY).subscribeOn(Schedulers.io())
                    .subscribe(::retrofitSuccess, ::retrofitError)
            SplashState.Loading(disposable::dispose)
        }
    }
}