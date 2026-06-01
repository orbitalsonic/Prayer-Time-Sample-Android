package com.orbitalsonic.prayertimesample.domain.repository

import com.orbitalsonic.prayertimesample.domain.model.PrayerName
import com.orbitalsonic.prayertimesample.domain.model.ScheduledPrayerAlarm

interface AlarmScheduler {
    fun schedulePrayer(alarm: ScheduledPrayerAlarm)
    fun cancelPrayer(prayer: PrayerName)
    fun cancelAll()
    suspend fun rescheduleAll()
    fun getActiveAlarmCount(): Int
}
