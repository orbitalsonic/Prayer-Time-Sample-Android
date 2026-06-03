package com.orbitalsonic.prayertimesample.data.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.orbitalsonic.prayertimesample.MainActivity
import com.orbitalsonic.prayertimesample.R
import com.orbitalsonic.prayertimesample.domain.model.PrayerName
import com.orbitalsonic.prayertimesample.domain.model.PrayerNotificationMode
import com.orbitalsonic.prayertimesample.receiver.NotificationDismissReceiver
import com.orbitalsonic.prayertimesample.receiver.PrayerStopReceiver

object PrayerNotificationHelper {

    const val CHANNEL_PRAYER = "prayer_times_channel"
    const val CHANNEL_AZAN = "prayer_azan_channel"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_PRAYER,
                context.getString(R.string.notification_channel_prayer),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_prayer_desc)
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_AZAN,
                context.getString(R.string.notification_channel_azan),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_azan_desc)
                setSound(null, null)
            }
        )
    }

    fun showPrayerNotification(
        context: Context,
        prayer: PrayerName,
        timeLabel: String,
        locationMessage: String,
        mode: PrayerNotificationMode
    ): Notification {
        ensureChannels(context)
        val notificationId = prayer.requestCode
        val channel = if (mode == PrayerNotificationMode.AZAN) CHANNEL_AZAN else CHANNEL_PRAYER

        val dismissIntent = Intent(context, NotificationDismissReceiver::class.java).apply {
            action = NotificationDismissReceiver.ACTION_DISMISS
            putExtra(NotificationDismissReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        val dismissPending = PendingIntent.getBroadcast(
            context,
            notificationId + 500,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(context, PrayerStopReceiver::class.java).apply {
            action = PrayerStopReceiver.ACTION_STOP_AZAN
        }
        val stopPending = PendingIntent.getBroadcast(
            context,
            notificationId + 600,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentIntent = PendingIntent.getActivity(
            context,
            notificationId,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = context.getString(
            R.string.prayer_notification_title,
            prayer.displayName,
            timeLabel.ifBlank { "--:--" }
        )
        val body = locationMessage.ifBlank {
            context.getString(R.string.location_unavailable)
        }

        val builder = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notification_prayer)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setDeleteIntent(dismissPending)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)

        if (mode == PrayerNotificationMode.AZAN) {
            builder.addAction(
                R.drawable.ic_stop,
                context.getString(R.string.stop_azan),
                stopPending
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                builder.setFullScreenIntent(contentIntent, true)
            }
        }

        val notification = builder.build()
        context.getSystemService(NotificationManager::class.java)
            .notify(notificationId, notification)
        return notification
    }

    fun cancel(context: Context, prayer: PrayerName) {
        context.getSystemService(NotificationManager::class.java)
            .cancel(prayer.requestCode)
    }

    fun cancelAll(context: Context) {
        PrayerName.entries.forEach { cancel(context, it) }
    }
}
