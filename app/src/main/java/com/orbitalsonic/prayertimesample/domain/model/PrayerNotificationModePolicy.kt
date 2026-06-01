package com.orbitalsonic.prayertimesample.domain.model

/**
 * Sunrise is not a prayer: Azan is never allowed.
 */
object PrayerNotificationModePolicy {

    fun effectiveMode(
        prayer: PrayerName,
        stored: PrayerNotificationMode
    ): PrayerNotificationMode {
        if (prayer == PrayerName.SUNRISE && stored == PrayerNotificationMode.AZAN) {
            return PrayerNotificationMode.NOTIFICATION_ONLY
        }
        return stored
    }

    fun nextMode(
        prayer: PrayerName,
        current: PrayerNotificationMode
    ): PrayerNotificationMode {
        val effective = effectiveMode(prayer, current)
        return when (prayer) {
            PrayerName.SUNRISE -> when (effective) {
                PrayerNotificationMode.DISABLED -> PrayerNotificationMode.NOTIFICATION_ONLY
                PrayerNotificationMode.NOTIFICATION_ONLY -> PrayerNotificationMode.DISABLED
                PrayerNotificationMode.AZAN -> PrayerNotificationMode.DISABLED
            }
            else -> when (effective) {
                PrayerNotificationMode.DISABLED -> PrayerNotificationMode.NOTIFICATION_ONLY
                PrayerNotificationMode.NOTIFICATION_ONLY -> PrayerNotificationMode.AZAN
                PrayerNotificationMode.AZAN -> PrayerNotificationMode.DISABLED
            }
        }
    }

    fun isAlarmEnabled(mode: PrayerNotificationMode): Boolean =
        mode != PrayerNotificationMode.DISABLED
}
