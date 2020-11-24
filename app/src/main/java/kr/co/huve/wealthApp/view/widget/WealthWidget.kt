package kr.co.huve.wealthApp.view.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import dagger.hilt.android.AndroidEntryPoint
import kr.co.huve.wealthApp.R
import kr.co.huve.wealthApp.util.TaskManager
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class WealthWidget : AppWidgetProvider() {

    @Inject
    lateinit var taskManager: TaskManager

    companion object {
        const val ManualUpdateAction = "MANUAL_UPDATE_ACTION"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (context != null && intent != null && ManualUpdateAction == intent.action) {
            // Apply the manual update
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, WealthWidget::class.java)
            onUpdate(context, manager, manager.getAppWidgetIds(component))
        }
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.wealth_widget)
    views.setTextViewText(R.id.appwidget_text, Date(System.currentTimeMillis()).toString())

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