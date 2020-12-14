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

    // WorkManager의 Worker가 background에서 foreground로 올라오는 경우 아주 간헐적으로 문제가 발생한다.
    // 작업 중단 -> background 작업 생성 -> foreground로 작업 승격 -> 작업 중단 -> ... -> Loop
    // WorkManager 신규 버전에서 Re-Initialize 버그는 해결되었지만, 아주 아주 간헐적으로 위 같은 버그가 내부적으로 발생하는 것으로 보인다.
    // 해당 버그가 해결될 때까지 임시로 직접 Foreground를 생성하도록 하고, 해당 버그가 해결되면 이 클래스를 제거하도록.
    override fun onCreate() {
        super.onCreate()
        val res = NotificationRes.LocationForeground(applicationContext)
        startForeground(res.getId(), notificationUtil.makeForegroundNotification(res, true))
    }
}