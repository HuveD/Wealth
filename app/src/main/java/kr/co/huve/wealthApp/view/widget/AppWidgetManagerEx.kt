package kr.co.huve.wealthApp.view.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.View
import android.widget.RemoteViews
import androidx.work.*
import kr.co.huve.wealthApp.R
import kr.co.huve.wealthApp.model.repository.data.CovidItem
import kr.co.huve.wealthApp.model.repository.data.DataKey
import kr.co.huve.wealthApp.model.repository.data.DayWeather
import kr.co.huve.wealthApp.model.repository.data.dust.Dust
import kr.co.huve.wealthApp.util.worker.WealthWidgetUpdateWorker
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

fun AppWidgetManager.requestWidgetUpdate(
    context: Context,
    views: RemoteViews,
    forcedUpdate: Boolean
) {
    Timber.d("Request widget work (forced:%s)", forcedUpdate)
    val workManager = WorkManager.getInstance(context)
    if (forcedUpdate) loadingView(context = context, views = views)
    workManager.beginUniqueWork(
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

fun AppWidgetManager.updateWidgetUi(
    context: Context,
    data: Bundle?,
    widgetIds: IntArray = getAppWidgetIds(ComponentName(context, WealthWidget::class.java))
) {
    // Apply the manual update
    for (appWidgetId in widgetIds) {
        updateAppWidget(context, appWidgetId, data)
    }
}

private fun AppWidgetManager.updateAppWidget(
    context: Context,
    appWidgetId: Int,
    bundleData: Bundle?
) {
    val views = RemoteViews(context.packageName, R.layout.wealth_widget)
    if (bundleData != null) {
        drawView(context = context, views = views, bundleData = bundleData)
    } else {
        requestWidgetUpdate(context = context, views = views, forcedUpdate = false)
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
    updateAppWidget(appWidgetId, views)
}

private fun AppWidgetManager.loadingView(context: Context, views: RemoteViews) {
    if (isUpdateDelayed(context) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(Intent(context, WidgetUpdateService::class.java))
    }
    views.setViewVisibility(R.id.labelContainer, View.GONE)
    views.setViewVisibility(R.id.weatherIcon, View.GONE)
    views.setViewVisibility(R.id.city, View.GONE)
    views.setViewVisibility(R.id.progress, View.VISIBLE)
    views.setTextViewText(R.id.currentTemp, context.getString(R.string.loading))

    // Update
    val component = ComponentName(context, WealthWidget::class.java)
    for (appWidgetId in getAppWidgetIds(component)) {
        updateAppWidget(appWidgetId, views)
    }
}

private fun isUpdateDelayed(context: Context): Boolean {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return powerManager.isPowerSaveMode
}

private fun drawView(context: Context, views: RemoteViews, bundleData: Bundle) {
    val weather = bundleData.getSerializable(DataKey.EXTRA_WEATHER_DATA.name) as DayWeather
    val covid = bundleData.getSerializable(DataKey.EXTRA_COVID_DATA.name) as CovidItem
    val dust = (bundleData.getSerializable(DataKey.EXTRA_DUST_DATA.name) as Dust).items.first()
    val city = bundleData.getSerializable(DataKey.EXTRA_CITY_NAME.name) as String
    views.setViewVisibility(R.id.progress, View.GONE)

    // Weather
    views.apply {
        // Weather
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
            String.format(
                context.getString(R.string.temp_with_symbol),
                weather.temp.roundToInt()
            )
        )

        // label container
        setViewVisibility(R.id.labelContainer, View.VISIBLE)

        // label pm10
        setTextViewText(
            R.id.pm10,
            getDustGrade(context, R.string.widget_pm10_label, dust.pm10Grade1h)
        )
        setInt(R.id.pm10, "setBackgroundResource", getDustBackground(dust.pm10Grade1h))

        // label pm 2.5
        setTextViewText(
            R.id.pm25,
            getDustGrade(context, R.string.widget_pm25_label, dust.pm25Grade1h)
        )
        setInt(R.id.pm25, "setBackgroundResource", getDustBackground(dust.pm25Grade1h))

        // label covid
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

private fun getDustGrade(context: Context, labelId: Int, grade: Int) =
    String.format(
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

private fun getDustBackground(grade: Int) = when (grade) {
    2 -> R.drawable.label_green
    3 -> R.drawable.label_orange
    4 -> R.drawable.label_red
    else -> R.drawable.label_blue
}