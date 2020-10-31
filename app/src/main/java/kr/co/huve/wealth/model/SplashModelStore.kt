package kr.co.huve.wealth.model

import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class SplashModelStore @Inject constructor() :
    ModelStore<SplashState>(SplashState.Idle)