package kr.co.huve.wealthApp.view.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import dagger.hilt.android.AndroidEntryPoint
import kr.co.huve.wealthApp.R
import kr.co.huve.wealthApp.util.NotificationUtil
import javax.inject.Inject

@AndroidEntryPoint
class WealthWidget : AppWidgetProvider() {

    @Inject
    lateinit var notificationUtil: NotificationUtil

    @Inject
    lateinit var widgetManager: AppWidgetManager

    companion object {
        const val ManualUpdateAction = "MANUAL_UPDATE_ACTION"
    }

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
        if (context != null) {
            val views = RemoteViews(context.packageName, R.layout.wealth_widget)
            widgetManager.requestWidgetUpdate(context = context, views = views, forcedUpdate = true)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        widgetManager.updateWidgetUi(context, null, appWidgetIds)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (context != null && intent != null) {
            when (intent.action) {
                ManualUpdateAction -> {
                    val views = RemoteViews(context.packageName, R.layout.wealth_widget)
                    widgetManager.requestWidgetUpdate(
                        context = context,
                        views = views,
                        forcedUpdate = true
                    )
                }
            }
        }
    }
}