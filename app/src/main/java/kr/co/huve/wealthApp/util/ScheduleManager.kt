package kr.co.huve.wealthApp.util

import android.content.Context
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kr.co.huve.wealthApp.util.data.DataKey
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
class ScheduleManager @Inject constructor(@ApplicationContext val context: Context) {
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
        Timber.d("scheduleMorningAlert at ${calendar.time}")
        val duration = calendar.timeInMillis - current

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
        WorkManager.getInstance(context).beginUniqueWork(
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
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DataKey.WORK_COVID_UPDATE.name,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequest
                .Builder(CovidUpdateCheckWorker::class.java, 15, TimeUnit.MINUTES)
                .setConstraints(config)
                .build()
        )
    }
}