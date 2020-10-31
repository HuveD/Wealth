package kr.co.huve.wealth.intent

import android.util.Log
import dagger.hilt.android.scopes.ActivityScoped
import io.reactivex.rxjava3.schedulers.Schedulers
import kr.co.huve.wealth.model.SplashModelStore
import kr.co.huve.wealth.model.SplashState
import kr.co.huve.wealth.model.backend.NetworkConfig
import kr.co.huve.wealth.model.backend.data.Weather
import kr.co.huve.wealth.model.backend.layer.CovidRestApi
import kr.co.huve.wealth.model.backend.layer.WeatherRestApi
import kr.co.huve.wealth.view.splash.SplashViewEvent
import retrofit2.HttpException
import javax.inject.Inject

@ActivityScoped
class SplashIntentFactory @Inject constructor(
    private val splashModelStore: SplashModelStore,
    private val weatherRestApi: WeatherRestApi,
    private val covidRestApi: CovidRestApi
) {

    fun process(event: SplashViewEvent) {
        splashModelStore.process(toIntent(event))
    }

    private fun toIntent(viewEvent: SplashViewEvent): Intent<SplashState> {
        return when (viewEvent) {
            SplashViewEvent.RequestWeatherFromActivity -> buildWeatherRequestIntent()
        }
    }

    private fun chainedIntent(block: SplashState.() -> SplashState) =
        splashModelStore.process(intent(block))

    private fun buildWeatherRequestIntent(): Intent<SplashState> {
        Log.d("정현", "buildWeatherRequestIntent")
        return intent {
            fun retrofitSuccess(loadedTasks: List<Weather>) = chainedIntent {
                SplashState.Ready(loadedTasks)
            }

            fun retrofitError(throwable: Throwable) = chainedIntent {
                if (throwable is HttpException) Log.d(
                    "정현",
                    throwable.response().toString()
                ) else Log.d("정현", throwable.toString())
                SplashState.Error(throwable)
            }

            val disposable =
                weatherRestApi.getCurrentWeather(NetworkConfig.WEATHER_KEY, "seoul", "ko", "metric")
                    .map { it.values.toList() }
                    .subscribeOn(Schedulers.io())
                    .subscribe(::retrofitSuccess, ::retrofitError)

            SplashState.Loading(disposable::dispose)
        }
    }
}