package kr.co.huve.wealthApp.model.splash

import dagger.hilt.android.scopes.ActivityScoped
import kr.co.huve.wealthApp.model.ModelStore
import javax.inject.Inject

@ActivityScoped
class SplashModelStore @Inject constructor() :
    ModelStore<SplashState>(SplashState.Idle)