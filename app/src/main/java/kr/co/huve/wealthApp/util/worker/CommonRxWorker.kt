package kr.co.huve.wealthApp.util.worker

import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.rxjava3.RxWorker
import kr.co.huve.wealthApp.model.repository.data.NotificationRes
import kr.co.huve.wealthApp.util.NotificationUtil

abstract class CommonRxWorker(
    private val appContext: Context,
    workerParams: WorkerParameters,
    var notificationUtil: NotificationUtil
) : RxWorker(appContext, workerParams) {

    protected fun createForegroundInfo(config: NotificationRes): ForegroundInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                config.getId(),
                notificationUtil.makeForegroundNotification(config, true),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            ForegroundInfo(
                config.getId(),
                notificationUtil.makeForegroundNotification(config, true)
            )
        }
    }

    protected fun stopForeground(intent: Intent) {
        appContext.stopService(intent)
    }
}