package com.orbitalsonic.prayertimesample.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.orbitalsonic.prayertimesample.PrayerTimeApp
import com.orbitalsonic.prayertimesample.domain.model.PrayerName
import com.orbitalsonic.prayertimesample.domain.model.PrayerNotificationMode
import com.orbitalsonic.prayertimesample.domain.model.PrayerType
import com.orbitalsonic.prayertimesample.domain.usecase.PrayerAlarmPlanner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PrayerAlarmReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_PRAYER_ALARM) return
        val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: return
        val prayer = runCatching { PrayerName.valueOf(prayerName) }.getOrNull() ?: return
        val scheduledTimeLabel = intent.getStringExtra(EXTRA_PRAYER_TIME).orEmpty()
        val scheduledLocationMessage = intent.getStringExtra(EXTRA_LOCATION_MESSAGE).orEmpty()

        val app = context.applicationContext as PrayerTimeApp
        val container = app.container

        val pendingResult = goAsync()
        scope.launch {
            try {
                handlePrayerTrigger(
                    context,
                    container,
                    prayer,
                    scheduledTimeLabel,
                    scheduledLocationMessage
                )
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handlePrayerTrigger(
        context: Context,
        container: com.orbitalsonic.prayertimesample.di.AppContainer,
        prayer: PrayerName,
        scheduledTimeLabel: String,
        scheduledLocationMessage: String
    ) {
        container.alarmScheduler.cancelPrayer(prayer)

        // Stop any in-progress Azan when a new prayer fires or notification is replaced.
        container.azanPlayerManager.stop()

        val settings = container.notificationSettingsRepository.getSettings()
        val mode = settings.modeFor(prayer)
        val today = container.prayerTimeRepository.getTodayPrayerTimes()
        val timeLabel = scheduledTimeLabel.ifBlank {
            today?.find(prayer)?.timeLabel.orEmpty()
        }
        val locationMessage = scheduledLocationMessage.ifBlank {
            container.locationRepository.getCachedLocation()
                ?.notificationMessage()
                .orEmpty()
        }

        when (mode) {
            PrayerNotificationMode.DISABLED -> Unit
            PrayerNotificationMode.NOTIFICATION_ONLY -> {
                com.orbitalsonic.prayertimesample.data.notification.PrayerNotificationHelper.showPrayerNotification(
                    context, prayer, timeLabel, locationMessage, mode
                )
            }
            PrayerNotificationMode.AZAN -> {
                if (prayer.prayerType == PrayerType.SUNRISE) {
                    com.orbitalsonic.prayertimesample.data.notification.PrayerNotificationHelper.showPrayerNotification(
                        context,
                        prayer,
                        timeLabel,
                        locationMessage,
                        PrayerNotificationMode.NOTIFICATION_ONLY
                    )
                } else {
                    com.orbitalsonic.prayertimesample.data.notification.PrayerNotificationHelper.showPrayerNotification(
                        context, prayer, timeLabel, locationMessage, mode
                    )
                    container.azanPlayerManager.play()
                }
            }
        }

        val now = System.currentTimeMillis()
        val tomorrow = container.prayerTimeRepository.getTomorrowPrayerTimes()
        val locationForSchedule = locationMessage.ifBlank {
            container.locationRepository.getCachedLocation()
                ?.notificationMessage()
                .orEmpty()
        }
        val next = PrayerAlarmPlanner.nextAlarmAfter(
            triggered = prayer,
            nowMillis = now,
            today = today,
            tomorrow = tomorrow,
            settings = settings,
            locationMessage = locationForSchedule
        ) ?: run {
            container.refreshPrayerTimesUseCase()
            val refreshedToday = container.prayerTimeRepository.getTodayPrayerTimes()
            val refreshedTomorrow = container.prayerTimeRepository.getTomorrowPrayerTimes()
            PrayerAlarmPlanner.nextAlarms(
                now,
                refreshedToday,
                refreshedTomorrow,
                settings,
                locationMessage = locationForSchedule,
                limit = 1
            ).firstOrNull()
        }

        next?.let { alarm ->
            val enriched = if (alarm.locationMessage.isNotBlank()) {
                alarm
            } else {
                alarm.copy(locationMessage = locationForSchedule)
            }
            container.alarmScheduler.schedulePrayer(enriched)
        }
    }

    companion object {
        const val ACTION_PRAYER_ALARM = "com.orbitalsonic.prayertimesample.PRAYER_ALARM"
        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_TRIGGER_AT = "extra_trigger_at"
        const val EXTRA_DAY_OFFSET = "extra_day_offset"
        const val EXTRA_PRAYER_TIME = "extra_prayer_time"
        const val EXTRA_LOCATION_MESSAGE = "extra_location_message"
    }
}
