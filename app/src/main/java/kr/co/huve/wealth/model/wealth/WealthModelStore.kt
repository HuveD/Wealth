package kr.co.huve.wealth.model.wealth

import kr.co.huve.wealth.model.ModelStore
import javax.inject.Inject

class WealthModelStore @Inject constructor() : ModelStore<WealthState>(WealthState.DisplayWeather) {
}