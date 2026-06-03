package com.orbitalsonic.prayertimesample.data.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.orbitalsonic.prayertimesample.domain.model.PrayerName
import com.orbitalsonic.prayertimesample.domain.model.ScheduledPrayerAlarm
import com.orbitalsonic.prayertimesample.domain.repository.AlarmScheduler
import com.orbitalsonic.prayertimesample.domain.repository.LocationRepository
import com.orbitalsonic.prayertimesample.domain.repository.NotificationSettingsRepository
import com.orbitalsonic.prayertimesample.domain.repository.PrayerTimeRepository
import com.orbitalsonic.prayertimesample.domain.usecase.PrayerAlarmPlanner
import com.orbitalsonic.prayertimesample.receiver.PrayerAlarmReceiver

class AlarmSchedulerImpl(
    private val context: Context,
    private val prayerTimeRepository: PrayerTimeRepository,
    private val locationRepository: LocationRepository,
    private val settingsRepository: NotificationSettingsRepository
) : AlarmScheduler {

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private val activeAlarms = mutableSetOf<PrayerName>()

    override fun schedulePrayer(alarm: ScheduledPrayerAlarm) {
        if (alarm.triggerAtMillis <= System.currentTimeMillis()) return
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = PrayerAlarmReceiver.ACTION_PRAYER_ALARM
            putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_NAME, alarm.prayer.name)
            putExtra(PrayerAlarmReceiver.EXTRA_TRIGGER_AT, alarm.triggerAtMillis)
            putExtra(PrayerAlarmReceiver.EXTRA_DAY_OFFSET, alarm.dayOffset)
            putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_TIME, alarm.timeLabel)
            putExtra(PrayerAlarmReceiver.EXTRA_LOCATION_MESSAGE, alarm.locationMessage)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.prayer.requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !alarmManager.canScheduleExactAlarms()
            ) {
                return
            }
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarm.triggerAtMillis,
                pendingIntent
            )
            activeAlarms.add(alarm.prayer)
        } catch (_: SecurityException) {
            // Exact alarm permission not granted
        }
    }

    override fun cancelPrayer(prayer: PrayerName) {
        val pendingIntent = buildPendingIntent(prayer)
        alarmManager.cancel(pendingIntent)
        activeAlarms.remove(prayer)
    }

    override fun cancelAll() {
        PrayerName.entries.forEach { cancelPrayer(it) }
        activeAlarms.clear()
    }

    override suspend fun rescheduleAll() {
        cancelAll()
        val now = System.currentTimeMillis()
        val today = prayerTimeRepository.getTodayPrayerTimes()
        val tomorrow = prayerTimeRepository.getTomorrowPrayerTimes()
        val settings = settingsRepository.getSettings()
        val locationMessage = locationRepository.getCachedLocation()
            ?.notificationMessage()
            .orEmpty()
        val alarms = PrayerAlarmPlanner.nextAlarms(
            now,
            today,
            tomorrow,
            settings,
            locationMessage = locationMessage
        )
        alarms.forEach { schedulePrayer(it) }
    }

    override fun getActiveAlarmCount(): Int = activeAlarms.size

    private fun buildPendingIntent(prayer: PrayerName): PendingIntent {
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = PrayerAlarmReceiver.ACTION_PRAYER_ALARM
            putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_NAME, prayer.name)
        }
        return PendingIntent.getBroadcast(
            context,
            prayer.requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
