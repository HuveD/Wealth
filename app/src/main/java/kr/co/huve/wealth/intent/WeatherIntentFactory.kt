package kr.co.huve.wealth.intent

import dagger.hilt.android.scopes.ActivityScoped
import kr.co.huve.wealth.model.wealth.WealthState
import kr.co.huve.wealth.view.splash.SplashViewEvent
import javax.inject.Inject

@ActivityScoped
class WeatherIntentFactory @Inject constructor(
) {
    fun process(event: SplashViewEvent) {

    }

    private fun makeIntent(viewEvent: SplashViewEvent): Intent<WealthState> {
        return intent { WealthState.DisplayWeather}
    }

    private fun chainedIntent(block: WealthState.() -> WealthState) {

    }
}
