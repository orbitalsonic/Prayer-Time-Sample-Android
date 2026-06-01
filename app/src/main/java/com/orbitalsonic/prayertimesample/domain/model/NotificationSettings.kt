package com.orbitalsonic.prayertimesample.domain.model

data class NotificationSettings(
    val globalEnabled: Boolean = true,
    val modes: Map<PrayerName, PrayerNotificationMode> = PrayerName.entries.associateWith {
        PrayerNotificationMode.NOTIFICATION_ONLY
    }
) {
    fun modeFor(prayer: PrayerName): PrayerNotificationMode {
        val stored = if (!globalEnabled) {
            PrayerNotificationMode.DISABLED
        } else {
            modes[prayer] ?: PrayerNotificationMode.DISABLED
        }
        return PrayerNotificationModePolicy.effectiveMode(prayer, stored)
    }

    fun isActive(prayer: PrayerName): Boolean =
        PrayerNotificationModePolicy.isAlarmEnabled(modeFor(prayer))
}
