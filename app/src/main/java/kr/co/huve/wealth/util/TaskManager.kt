package kr.co.huve.wealth.util

import android.content.Context
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kr.co.huve.wealth.util.data.DataKey
import kr.co.huve.wealth.util.worker.CovidAlertCheckWorker
import kr.co.huve.wealth.util.worker.DailyNotificationWorker
import kr.co.huve.wealth.util.worker.WealthAlertCheckWorker
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskManager @Inject constructor(@ApplicationContext val context: Context) {
    fun scheduleMorningAlert() {
        // Set time
        val morningHour = 7
        val calendar = Calendar.getInstance()
        val current = calendar.time.time
        calendar.set(Calendar.HOUR_OF_DAY, morningHour)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        // Calculate duration
        if (calendar.time.time <= current) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        val duration = calendar.time.time - current

        // Create worker
        val config = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val weatherRequest = OneTimeWorkRequest.Builder(WealthAlertCheckWorker::class.java)
            .setInitialDelay(duration, TimeUnit.MILLISECONDS).setConstraints(config).build()
        val covidRequest = OneTimeWorkRequest.Builder(CovidAlertCheckWorker::class.java)
            .setInitialDelay(duration, TimeUnit.MILLISECONDS).setConstraints(config).build()

        // Scheduled chained task
        Timber.d("scheduleMorningAlert at ${calendar.time}")
        WorkManager.getInstance(context).beginUniqueWork(
            DataKey.WORK_NOTIFICATION.name,
            ExistingWorkPolicy.REPLACE, listOf(weatherRequest, covidRequest)
        ).then(
            OneTimeWorkRequest.Builder(DailyNotificationWorker::class.java).build()
        ).enqueue()
    }
}