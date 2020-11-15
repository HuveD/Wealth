package kr.co.huve.wealthApp.model.wealth

import dagger.hilt.android.scopes.ActivityScoped
import kr.co.huve.wealthApp.model.ModelStore
import javax.inject.Inject

@ActivityScoped
class WealthModelStore @Inject constructor() :
    ModelStore<WealthState>(WealthState.IDLE) {
}