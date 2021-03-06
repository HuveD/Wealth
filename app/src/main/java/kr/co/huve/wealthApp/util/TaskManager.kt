package kr.co.huve.wealthApp.util

import android.content.Context
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kr.co.huve.wealthApp.model.repository.data.DataKey
import kr.co.huve.wealthApp.util.worker.CovidUpdateCheckWorker
import kr.co.huve.wealthApp.util.worker.DailyNotificationWorker
import kr.co.huve.wealthApp.util.worker.MaskCheckWorker
import kr.co.huve.wealthApp.util.worker.UmbrellaCheckWorker
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskManager @Inject constructor(
    @ApplicationContext val context: Context,
    private val workManager: WorkManager
) {
    fun scheduleMorningAlert() {
        // Set time
        val morningHour = 7
        val calendar = Calendar.getInstance()
        val current = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, morningHour)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        // Calculate duration
        if (calendar.timeInMillis <= current) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        val duration = calendar.timeInMillis - current
        Timber.d("scheduleMorningAlert at ${calendar.time}. Will be execute in ${duration / 1000 / 60} minute later")

        // Create weather worker
        val config = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val weatherRequest = OneTimeWorkRequest.Builder(UmbrellaCheckWorker::class.java)
            .setInitialDelay(duration, TimeUnit.MILLISECONDS)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .setConstraints(config)
            .build()

        // Create covid worker
        val covidRequest = OneTimeWorkRequest.Builder(MaskCheckWorker::class.java)
            .setInitialDelay(duration, TimeUnit.MILLISECONDS)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .setConstraints(config)
            .build()

        // Scheduled chained task
        workManager.beginUniqueWork(
            DataKey.WORK_NOTIFICATION.name,
            ExistingWorkPolicy.REPLACE, listOf(weatherRequest, covidRequest)
        ).then(
            OneTimeWorkRequest.Builder(DailyNotificationWorker::class.java).build()
        ).enqueue()
    }

    fun scheduleCovidUpdateCheck() {
        // Create covid worker
        val config = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        // Schedule update checker
        workManager.enqueueUniquePeriodicWork(
            DataKey.WORK_COVID_UPDATED.name,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequest
                .Builder(CovidUpdateCheckWorker::class.java, 15, TimeUnit.MINUTES)
                .setConstraints(config)
                .build()
        )
    }
}