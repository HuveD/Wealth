package kr.co.huve.wealthApp.view.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import androidx.work.*
import dagger.hilt.android.AndroidEntryPoint
import kr.co.huve.wealthApp.R
import kr.co.huve.wealthApp.util.TaskManager
import kr.co.huve.wealthApp.util.data.DataKey
import kr.co.huve.wealthApp.util.repository.network.data.CovidItem
import kr.co.huve.wealthApp.util.repository.network.data.DayWeather
import kr.co.huve.wealthApp.util.repository.network.data.dust.Dust
import kr.co.huve.wealthApp.util.worker.WealthWidgetUpdateWorker
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.roundToInt

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
        if (context != null) {
            val views = RemoteViews(context.packageName, R.layout.wealth_widget)
            requestWorks(context = context, views = views, forcedUpdate = true)
        }
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
                    val views = RemoteViews(context.packageName, R.layout.wealth_widget)
                    requestWorks(context = context, views = views, forcedUpdate = true)
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

private fun requestWorks(context: Context, views: RemoteViews, forcedUpdate: Boolean) {
    Timber.d("Request widget work (forced:%s)", forcedUpdate)
    if (forcedUpdate) loadingView(context = context, views = views)
    WorkManager.getInstance(context).beginUniqueWork(
        DataKey.WORK_UPDATE_WIDGET.name,
        ExistingWorkPolicy.REPLACE,
        OneTimeWorkRequest.Builder(WealthWidgetUpdateWorker::class.java)
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
    val views = RemoteViews(context.packageName, R.layout.wealth_widget)
    if (intent?.action == WealthWidget.InvalidateAction) {
        drawView(context = context, views = views, intent = intent)
    } else {
        requestWorks(context = context, views = views, forcedUpdate = false)
    }

    // Request when parent view clicked
    val pendingIntent: PendingIntent = Intent(
        context,
        WealthWidget::class.java
    ).run {
        this.action = WealthWidget.ManualUpdateAction
        PendingIntent.getBroadcast(context, 0, this, PendingIntent.FLAG_UPDATE_CURRENT)
    }
    views.setOnClickPendingIntent(R.id.parent, pendingIntent)

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}

internal fun loadingView(context: Context, views: RemoteViews) {
    views.setViewVisibility(R.id.labelContainer, View.GONE)
    views.setViewVisibility(R.id.weatherIcon, View.GONE)
    views.setViewVisibility(R.id.city, View.GONE)
    views.setViewVisibility(R.id.progress, View.VISIBLE)
    views.setTextViewText(R.id.currentTemp, context.getString(R.string.loading))

    // Update
    val manager = AppWidgetManager.getInstance(context)
    val component = ComponentName(context, WealthWidget::class.java)
    for (appWidgetId in manager.getAppWidgetIds(component)) {
        manager.updateAppWidget(appWidgetId, views)
    }
}

internal fun drawView(context: Context, views: RemoteViews, intent: Intent) {
    val weather = intent.getSerializableExtra(DataKey.EXTRA_WEATHER_DATA.name) as DayWeather
    val covid = intent.getSerializableExtra(DataKey.EXTRA_COVID_DATA.name) as CovidItem
    val dust = (intent.getSerializableExtra(DataKey.EXTRA_DUST_DATA.name) as Dust).items.first()
    val city = intent.getSerializableExtra(DataKey.EXTRA_CITY_NAME.name) as String
    views.setViewVisibility(R.id.progress, View.GONE)

    // Weather
    views.apply {
        val icon = weather.weatherInfo.first().getWeatherIcon(isTitle = false)
        setViewVisibility(R.id.weatherIcon, View.VISIBLE)
        views.setViewVisibility(R.id.city, View.VISIBLE)
        setImageViewResource(R.id.weatherIcon, icon)
        setTextViewText(
            R.id.city,
            if (city.isEmpty()) context.getString(R.string.working) else city
        )
        setTextViewText(
            R.id.currentTemp,
            String.format(context.getString(R.string.temp_with_symbol), weather.temp.roundToInt())
        )
    }

    // label
    views.setViewVisibility(R.id.labelContainer, View.VISIBLE)
    // pm 10
    views.apply {
        setTextViewText(
            R.id.pm10,
            getDustGrade(context, R.string.widget_pm10_label, dust.pm10Grade1h)
        )
        setInt(R.id.pm10, "setBackgroundResource", getDustBackground(dust.pm10Grade1h))
    }
    // pm 2.5
    views.apply {
        setTextViewText(
            R.id.pm25,
            getDustGrade(context, R.string.widget_pm25_label, dust.pm25Grade1h)
        )
        setInt(R.id.pm25, "setBackgroundResource", getDustBackground(dust.pm25Grade1h))
    }
    // covid
    views.apply {
        val increasedCount = covid.increasedCount
        setTextViewText(
            R.id.covid,
            String.format(context.getString(R.string.widget_covid_label), increasedCount)
        )
        setInt(
            R.id.covid, "setBackgroundResource", when {
                covid.increasedCount > 300 -> R.drawable.label_red
                covid.increasedCount > 0 -> R.drawable.label_orange
                else -> R.drawable.label_green
            }
        )
    }
}

internal fun getDustGrade(context: Context, labelId: Int, grade: Int) = String.format(
    context.getString(
        labelId, when (grade) {
            1 -> context.getString(R.string.grade_good)
            2 -> context.getString(R.string.grade_normal)
            3 -> context.getString(R.string.grade_bad)
            4 -> context.getString(R.string.grade_too_bad)
            else -> context.getString(R.string.working)
        }
    )
)

internal fun getDustBackground(grade: Int) = when (grade) {
    2 -> R.drawable.label_green
    3 -> R.drawable.label_orange
    4 -> R.drawable.label_red
    else -> R.drawable.label_blue
}