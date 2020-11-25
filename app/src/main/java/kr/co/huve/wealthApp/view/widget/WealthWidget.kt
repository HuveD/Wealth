package kr.co.huve.wealthApp.view.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.work.*
import dagger.hilt.android.AndroidEntryPoint
import kr.co.huve.wealthApp.R
import kr.co.huve.wealthApp.model.backend.data.CovidItem
import kr.co.huve.wealthApp.model.backend.data.DayWeather
import kr.co.huve.wealthApp.model.backend.data.dust.DustItem
import kr.co.huve.wealthApp.util.TaskManager
import kr.co.huve.wealthApp.util.data.DataKey
import kr.co.huve.wealthApp.util.worker.WidgetUpdateWorker
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class WealthWidget : AppWidgetProvider() {

    @Inject
    lateinit var taskManager: TaskManager

    companion object {
        const val ManualUpdateAction = "MANUAL_UPDATE_ACTION"
        const val InvalidateAction = "INVALIDATE_ACTION"
    }

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
        if (context != null) requestWorks(context = context)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, null)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (context != null && intent != null) {
            when (intent.action) {
                ManualUpdateAction -> {
                    requestWorks(context)
                }
                InvalidateAction -> {
                    // Apply the manual update
                    val manager = AppWidgetManager.getInstance(context)
                    val component = ComponentName(context, WealthWidget::class.java)
                    for (appWidgetId in manager.getAppWidgetIds(component)) {
                        updateAppWidget(context, manager, appWidgetId, intent)
                    }
                }
            }
        }
    }
}

private fun requestWorks(context: Context) {
    WorkManager.getInstance(context).beginUniqueWork(
        DataKey.WORK_UPDATE_WIDGET.name,
        ExistingWorkPolicy.REPLACE,
        OneTimeWorkRequest.Builder(WidgetUpdateWorker::class.java)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            ).setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            ).build()
    ).enqueue()
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    intent: Intent?
) {
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.wealth_widget)
    views.setTextViewText(R.id.appwidget_text, Date(System.currentTimeMillis()).toString())

    intent?.run {
        val weather = intent.getSerializableExtra(DataKey.EXTRA_WEATHER_DATA.name) as DayWeather
        val covid = intent.getSerializableExtra(DataKey.EXTRA_COVID_DATA.name) as CovidItem
        val dust = intent.getSerializableExtra(DataKey.EXTRA_DUST_DATA.name) as DustItem
        val sb = StringBuilder()
        sb.append(weather.weatherInfo.first().getWeatherDescription(context))
        sb.append("\n코로나: ")
        sb.append(covid.increasedCount)
        sb.append("\n미세먼지: ")
        sb.append(dust.pm10Grade1h)
        views.setTextViewText(R.id.appwidget_text, sb.toString())
    }

    // Request
    val pendingIntent: PendingIntent = Intent(
        context,
        WealthWidget::class.java
    ).run {
        this.action = WealthWidget.ManualUpdateAction
        PendingIntent.getBroadcast(context, 0, this, PendingIntent.FLAG_UPDATE_CURRENT)
    }
    views.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent)

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}