package kr.co.huve.wealthApp.view.widget

import android.app.Service
import android.content.Intent
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import kr.co.huve.wealthApp.model.repository.data.NotificationRes
import kr.co.huve.wealthApp.util.NotificationUtil
import javax.inject.Inject

@AndroidEntryPoint
class WidgetUpdateService : Service() {
    @Inject
    lateinit var notificationUtil: NotificationUtil
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onCreate() {
        super.onCreate()
        val res = NotificationRes.LocationForeground(applicationContext)
        startForeground(res.getId(), notificationUtil.makeForegroundNotification(res, true))
    }
}