package kr.co.huve.wealthApp.util.data

import android.content.Context
import kr.co.huve.wealthApp.R


sealed class NotificationRes : NotificationInfo {
    data class CommonNotification(
        val context: Context,
        val titleName: String,
        val message: String,
        val notificationId: Int
    ) : NotificationRes() {
        override fun getChannelId(): String {
            return context.getString(R.string.common_alert_id)
        }

        override fun getChannelName(): String {
            return context.getString(R.string.common_alert_name)
        }

        override fun getChannelDescription(): String {
            return context.getString(R.string.common_alert_description)
        }

        override fun getTitle(): String {
            return titleName
        }

        override fun getContent(): String {
            return message
        }

        override fun getId(): Int {
            return notificationId
        }
    }

    data class LocationForeground(val context: Context) : NotificationRes() {
        override fun getChannelId(): String = context.getString(R.string.daily_alert_id)

        override fun getChannelName(): String = context.getString(R.string.daily_alert_name)

        override fun getChannelDescription(): String =
            context.getString(R.string.daily_alert_description)

        override fun getTitle(): String = context.getString(R.string.location_foreground_title)

        override fun getContent(): String = context.getString(R.string.location_foreground_content)
    }

    data class DailyAlert(
        val context: Context,
        val description: String,
        val umbrella: Boolean,
        val mask: Boolean
    ) : NotificationRes() {
        override fun getChannelId(): String = context.getString(R.string.daily_alert_id)

        override fun getChannelName(): String = context.getString(R.string.daily_alert_name)

        override fun getChannelDescription(): String =
            context.getString(R.string.daily_alert_description)

        override fun getTitle(): String = context.getString(R.string.daily_notification_title)

        override fun getContent(): String {
            val sb = StringBuilder()
            sb.append(description)
            sb.append(
                when {
                    umbrella && mask -> context.getString(R.string.task_mask_and_umbrella)
                    umbrella -> context.getString(R.string.take_umbrella)
                    mask -> context.getString(R.string.take_mask)
                    else -> context.getString(R.string.have_nothing_to_do)
                }
            )
            return sb.toString()
        }
    }

    data class CovidUpdate(val context: Context) : NotificationRes() {
        override fun getChannelId(): String {
            return context.getString(R.string.check_covid_update_id)
        }

        override fun getChannelName(): String {
            return context.getString(R.string.check_covid_update_name)
        }

        override fun getChannelDescription(): String {
            return context.getString(R.string.check_covid_update_description)
        }

        override fun getTitle(): String {
            return context.getString(R.string.update_covid_title)
        }

        override fun getContent(): String {
            return context.getString(R.string.update_covid_content)
        }
    }

    open fun getId(): Int = ordinal()
}

private fun NotificationRes.ordinal(): Int =
    NotificationRes::class.java.classes.indexOfFirst { it == this.javaClass }

private interface NotificationInfo {
    fun getChannelId(): String
    fun getChannelName(): String
    fun getChannelDescription(): String
    fun getTitle(): String
    fun getContent(): String
}