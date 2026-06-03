package com.orbitalsonic.prayertimesample.domain.model

data class ScheduledPrayerAlarm(
    val prayer: PrayerName,
    val triggerAtMillis: Long,
    val dayOffset: Int = 0,
    val timeLabel: String = "",
    val locationMessage: String = ""
)
