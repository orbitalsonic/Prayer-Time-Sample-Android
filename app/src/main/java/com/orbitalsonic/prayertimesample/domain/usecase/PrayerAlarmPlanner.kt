package com.orbitalsonic.prayertimesample.domain.usecase

import com.orbitalsonic.prayertimesample.domain.model.NotificationSettings
import com.orbitalsonic.prayertimesample.domain.model.PrayerDayTimes
import com.orbitalsonic.prayertimesample.domain.model.PrayerName
import com.orbitalsonic.prayertimesample.domain.model.ScheduledPrayerAlarm

/**
 * Rolling alarm strategy: returns at most [MAX_ACTIVE_ALARMS] upcoming enabled prayers.
 * On each trigger the receiver schedules the next pending prayer.
 */
object PrayerAlarmPlanner {
    const val MAX_ACTIVE_ALARMS = 5

    fun nextAlarms(
        nowMillis: Long,
        today: PrayerDayTimes?,
        tomorrow: PrayerDayTimes?,
        settings: NotificationSettings,
        locationMessage: String = "",
        limit: Int = MAX_ACTIVE_ALARMS
    ): List<ScheduledPrayerAlarm> {
        val candidates = mutableListOf<Pair<com.orbitalsonic.prayertimesample.domain.model.PrayerTimeModel, Int>>()
        today?.prayers?.forEach { candidates.add(it to 0) }
        tomorrow?.prayers?.forEach { candidates.add(it to 1) }
        return candidates
            .filter { (prayer, _) ->
                settings.isActive(prayer.name) && prayer.timeMillis > nowMillis
            }
            .sortedBy { (prayer, dayOffset) -> prayer.timeMillis + dayOffset * 86_400_000L }
            .take(limit)
            .map { (prayer, dayOffset) ->
                ScheduledPrayerAlarm(
                    prayer = prayer.name,
                    triggerAtMillis = prayer.timeMillis,
                    dayOffset = dayOffset,
                    timeLabel = prayer.timeLabel,
                    locationMessage = locationMessage
                )
            }
    }

    fun nextAlarmAfter(
        triggered: PrayerName,
        nowMillis: Long,
        today: PrayerDayTimes?,
        tomorrow: PrayerDayTimes?,
        settings: NotificationSettings,
        locationMessage: String = ""
    ): ScheduledPrayerAlarm? {
        val all = nextAlarms(
            nowMillis,
            today,
            tomorrow,
            settings,
            locationMessage = locationMessage,
            limit = 12
        )
        val order = PrayerName.entries
        val triggeredIndex = order.indexOf(triggered)
        return all.firstOrNull { alarm ->
            val idx = order.indexOf(alarm.prayer)
            idx > triggeredIndex || (triggered == PrayerName.ISHA && alarm.dayOffset == 1)
        } ?: all.firstOrNull()
    }

}
