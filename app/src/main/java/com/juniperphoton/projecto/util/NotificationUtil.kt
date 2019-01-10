package com.juniperphoton.projecto.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.juniperphoton.projecto.App
import com.juniperphoton.projecto.R

object NotificationUtil {
    const val DEFAULT_NOTIFICATION_CHANNEL_ID = "default"

    private const val DEFAULT_ID = 0

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel0 = NotificationChannel(DEFAULT_NOTIFICATION_CHANNEL_ID,
                    App.instance.getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_DEFAULT)
            channel0.enableLights(true)
            channel0.enableVibration(true)

            val channels = arrayListOf(channel0)
            getNotificationManager(App.instance).createNotificationChannels(channels)
        }
    }

    fun show(notification: Notification) {
        getNotificationManager(App.instance)
                .notify(notification.hashCode(), notification)
    }

    private fun getNotificationManager(ctx: Context): NotificationManager {
        return ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
}