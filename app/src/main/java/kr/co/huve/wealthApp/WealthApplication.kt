package kr.co.huve.wealthApp

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import kr.co.huve.wealthApp.util.TaskManager
import kr.co.huve.wealthApp.util.repository.database.dao.PlaceDao
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class WealthApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var taskManager: TaskManager

    @Inject
    lateinit var placeDao: PlaceDao

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        taskManager.scheduleMorningAlert()
    }

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder().setWorkerFactory(workerFactory).build()
}