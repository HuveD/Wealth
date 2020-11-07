package kr.co.huve.wealth.intent

import com.google.gson.Gson
import dagger.hilt.android.scopes.ActivityScoped
import io.reactivex.rxjava3.schedulers.Schedulers
import kr.co.huve.wealth.model.backend.NetworkConfig
import kr.co.huve.wealth.model.backend.data.CovidResult
import kr.co.huve.wealth.model.backend.layer.CovidRestApi
import kr.co.huve.wealth.model.wealth.WealthModelStore
import kr.co.huve.wealth.model.wealth.WealthState
import kr.co.huve.wealth.view.main.WealthViewEvent
import org.json.XML
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject

@ActivityScoped
class WealthIntentFactory @Inject constructor(
    private val modelStore: WealthModelStore,
    private val covidRestApi: CovidRestApi,
    private val gson: Gson
) {
    fun process(event: WealthViewEvent) {
        modelStore.process(makeIntent(event))
    }

    private fun makeIntent(viewEvent: WealthViewEvent): Intent<WealthState> {
        return when (viewEvent) {
            is WealthViewEvent.PageChanged -> buildFragmentSelectedIntent(position = viewEvent.index)
            is WealthViewEvent.WeatherTabChanged -> buildWeatherTabChangeIntent(viewEvent.isHour)
            is WealthViewEvent.InvalidateStone -> buildInvalidateStoneIntent()
            is WealthViewEvent.RequestCovid -> buildRequestCovidIntent(viewEvent.dateString)
        }
    }

    private fun chainedIntent(block: WealthState.() -> WealthState) =
        modelStore.process(intent(block))

    private fun buildFragmentSelectedIntent(position: Int): Intent<WealthState> {
        return intent { WealthState.FragmentSelected(position) }
    }

    private fun buildWeatherTabChangeIntent(isHour: Boolean): Intent<WealthState> {
        return intent { WealthState.WeatherTabChanged(isHour) }
    }

    private fun buildInvalidateStoneIntent(): Intent<WealthState> {
        return intent { WealthState.InvalidateStone }
    }

    private fun buildRequestCovidIntent(dateString: String): Intent<WealthState> {
        return intent {
            fun retrofitSuccess(response: String) = chainedIntent {
                WealthState.CovidDataReceived(
                    data = gson.fromJson(
                        XML.toJSONObject(response).toString(),
                        CovidResult::class.java
                    ).getItemList()
                )
            }

            fun retrofitError(t: Throwable) = chainedIntent {
                if (t is HttpException) Timber.d(
                    t.response().toString()
                ) else Timber.d(t.toString())
                WealthState.CovidRequestFail
            }

            WealthState.RequestCovid(
                covidRestApi.getCovidStatus(
                    NetworkConfig.COVID_KEY,
                    1,
                    20,
                    dateString,
                    dateString
                ).subscribeOn(Schedulers.io()).subscribe(::retrofitSuccess, ::retrofitError)
            )
        }
    }
}
