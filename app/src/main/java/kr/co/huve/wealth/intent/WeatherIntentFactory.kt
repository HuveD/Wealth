package kr.co.huve.wealth.intent

import dagger.hilt.android.scopes.ActivityScoped
import kr.co.huve.wealth.model.wealth.WealthModelStore
import kr.co.huve.wealth.model.wealth.WealthState
import kr.co.huve.wealth.view.main.WealthViewEvent
import javax.inject.Inject

@ActivityScoped
class WeatherIntentFactory @Inject constructor(
    private val modelStore: WealthModelStore
) {
    fun process(event: WealthViewEvent) {
        modelStore.process(makeIntent(event))
    }

    private fun makeIntent(viewEvent: WealthViewEvent): Intent<WealthState> {
        return when (viewEvent) {
            is WealthViewEvent.PageChanged -> buildFragmentSelectedIntent(position = viewEvent.index)
        }
    }

    private fun buildFragmentSelectedIntent(position: Int): Intent<WealthState> {
        return intent { WealthState.FragmentSelected(position) }
    }
}
