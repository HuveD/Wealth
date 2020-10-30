package kr.co.huve.wealth.intent

import android.util.Log
import kr.co.huve.wealth.model.SplashModelStore
import kr.co.huve.wealth.model.SplashState
import kr.co.huve.wealth.view.splash.SplashViewEvent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SplashIntentFactory @Inject constructor(
    private val splashModelStore: SplashModelStore) {

    fun process(event:SplashViewEvent) {
        splashModelStore.process(toIntent(event))
    }

    private fun toIntent(viewEvent: SplashViewEvent): Intent<SplashState> {
        return when(viewEvent) {
            SplashViewEvent.StartMainActivity -> {
                moveMainActivity()
            }
        }
    }

    private fun buildReloadTasksIntent(): Intent<TasksState> {
        return intent {

            assert(syncState == IDLE)

            fun retrofitSuccess(loadedTasks:List<Task>) = chainedIntent {
                assert(syncState is PROCESS && syncState.type == REFRESH)
                copy(tasks = loadedTasks, syncState = IDLE)
            }

            fun retrofitError(throwable:Throwable) = chainedIntent {
                assert(syncState is PROCESS && syncState.type == REFRESH)
                copy(syncState = ERROR(throwable))
            }

            val disposable = tasksRestApi.getTasks()
                .map { it.values.toList() }
                .subscribeOn(Schedulers.io())
                .subscribe(::retrofitSuccess, ::retrofitError)

            copy( syncState = PROCESS(REFRESH, disposable::dispose))
        }
    }
}