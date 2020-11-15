package kr.co.huve.wealthApp.util.worker

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.WorkerParameters
import androidx.work.rxjava3.RxWorker
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import kr.co.huve.wealthApp.util.NotificationUtil
import kr.co.huve.wealthApp.util.ScheduleManager
import kr.co.huve.wealthApp.util.data.DataKey
import kr.co.huve.wealthApp.util.data.NotificationRes

class DailyNotificationWorker @WorkerInject constructor(
    @Assisted val appContext: Context,
    @Assisted val workerParams: WorkerParameters,
    private val scheduleManager: ScheduleManager,
    private val notificationUtil: NotificationUtil
) : RxWorker(appContext, workerParams) {
    override fun createWork(): Single<Result> {
        return Single.fromObservable(
            Observable.create<Result.Success> {
                val description = inputData.getString(DataKey.WORK_WEATHER_DESCRIPTION.name)
                val umbrella = inputData.getBoolean(DataKey.WORK_NEED_UMBRELLA.name, false)
                val mask = inputData.getBoolean(DataKey.WORK_NEED_MASK.name, false)

                // Notify message
                notificationUtil.makeNotification(
                    res = NotificationRes.DailyAlert(appContext, description ?: "", umbrella, mask)
                )

                // Start Covid Update Check
                scheduleManager.scheduleCovidUpdateCheck()
                // Schedule tomorrow alert
                scheduleManager.scheduleMorningAlert()
                Result.success()
            }
        )
    }
}