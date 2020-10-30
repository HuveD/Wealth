package kr.co.huve.wealth.model

import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject
import javax.inject.Singleton

@ActivityScoped
class SplashModelStore @Inject constructor() : ModelStore<SplashState>(SplashState.Loading)