package kr.co.huve.wealth.util.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.WorkerParameters
import androidx.work.rxjava3.RxWorker
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import kr.co.huve.wealth.R
import kr.co.huve.wealth.util.TaskManager
import kr.co.huve.wealth.util.data.DataKey

class DailyNotificationWorker @WorkerInject constructor(
    @Assisted val appContext: Context,
    @Assisted val workerParams: WorkerParameters,
    private val taskManager: TaskManager
) : RxWorker(appContext, workerParams) {
    override fun createWork(): Single<Result> {
        return Single.fromObservable(
            Observable.create<Result.Success> {
                val description = inputData.getString(DataKey.WORK_WEATHER_DESCRIPTION.name)
                val umbrella = inputData.getBoolean(DataKey.WORK_NEED_UMBRELLA.name, false)
                val mask = inputData.getBoolean(DataKey.WORK_NEED_MASK.name, false)
                val content = getContent(description ?: "", umbrella, mask)

                // Make chanel for lately version
                createNotificationChannel()
                // Notify message
                makeNotification(content)

                // Schedule tomorrow alert
                taskManager.scheduleMorningAlert()
                Result.success()
            }
        )
    }

    private fun makeNotification(content: String) {
        var builder =
            NotificationCompat.Builder(appContext, appContext.getString(R.string.daily_alert_id))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(appContext.getString(R.string.daily_notification))
                .setContentText(content)
                .setStyle(NotificationCompat.BigTextStyle().bigText(content))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(appContext)) {
            notify(0, builder.build())
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(
                appContext.getString(R.string.daily_alert_id),
                appContext.getString(R.string.daily_alert_name),
                importance
            ).apply {
                description = appContext.getString(R.string.daily_alert_description)
            }

            // Register the channel with the system
            val manager: NotificationManager =
                appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun getContent(description: String, umbrella: Boolean, mask: Boolean): String {
        val sb = StringBuilder()
        sb.append(description)
        sb.append(
            when {
                umbrella && mask -> appContext.getString(R.string.task_mask_and_umbrella)
                umbrella -> appContext.getString(R.string.take_umbrella)
                mask -> appContext.getString(R.string.take_mask)
                else -> appContext.getString(R.string.have_nothing_to_do)
            }
        )
        return sb.toString()
    }
}