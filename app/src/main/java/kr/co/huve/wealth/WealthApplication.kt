package kr.co.huve.wealth

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import kr.co.huve.wealth.util.TaskManager
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class WealthApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var taskManager: TaskManager

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        taskManager.scheduleMorningAlert()
    }

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder().setWorkerFactory(workerFactory).build()
}