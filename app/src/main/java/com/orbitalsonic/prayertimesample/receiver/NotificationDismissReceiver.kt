package com.orbitalsonic.prayertimesample.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.orbitalsonic.prayertimesample.PrayerTimeApp

class NotificationDismissReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_DISMISS) return
        val app = context.applicationContext as PrayerTimeApp
        app.container.azanPlayerManager.stop()
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        if (notificationId != -1) {
            context.getSystemService(android.app.NotificationManager::class.java)
                .cancel(notificationId)
        }
    }

    companion object {
        const val ACTION_DISMISS = "com.orbitalsonic.prayertimesample.NOTIFICATION_DISMISS"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }
}
