package com.orbitalsonic.prayertimesample.domain.model

enum class PrayerNotificationMode {
    DISABLED,
    NOTIFICATION_ONLY,
    AZAN;

    companion object {
        fun fromOrdinal(value: Int): PrayerNotificationMode =
            entries.getOrElse(value) { DISABLED }
    }
}
