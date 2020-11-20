package kr.co.huve.wealthApp.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kr.co.huve.wealthApp.R
import kr.co.huve.wealthApp.util.data.NotificationRes
import kr.co.huve.wealthApp.view.splash.SplashActivity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationUtil @Inject constructor(@ApplicationContext private val context: Context) {

    fun makeNotification(
        res: NotificationRes
    ) {
        val intent = Intent(context, SplashActivity::class.java)
        makeNotification(res, intent)
    }

    fun makeNotification(
        res: NotificationRes,
        intent: Intent
    ) {
        createNotificationChannel(res)
        intent.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        with(NotificationManagerCompat.from(context)) {
            notify(res.getId(), getBuilder(res, pendingIntent).build())
        }
    }

    fun makeForegroundNotification(
        res: NotificationRes
    ): Notification {
        createNotificationChannel(res)
        return getForegroundBuilder(res).build()
    }

    private fun createNotificationChannel(res: NotificationRes) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(
                res.getChannelId(),
                res.getChannelName(),
                importance
            ).apply {
                description = res.getChannelDescription()
            }

            // Register the channel with the system
            val manager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun getBuilder(
        res: NotificationRes,
        intent: PendingIntent
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, res.getChannelId())
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(res.getTitle())
            .setContentText(res.getContent())
            .setStyle(NotificationCompat.BigTextStyle().bigText(res.getContent()))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(intent)
            .setAutoCancel(true)
    }

    private fun getForegroundBuilder(
        res: NotificationRes,
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, res.getChannelId())
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(res.getTitle())
            .setContentText(res.getContent())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
    }
}