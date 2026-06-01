package com.orbitalsonic.prayertimesample.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class PrayerNotificationModePolicyTest {

    @Test
    fun sunrise_azan_fallsBackToNotificationOnly() {
        val mode = PrayerNotificationModePolicy.effectiveMode(
            PrayerName.SUNRISE,
            PrayerNotificationMode.AZAN
        )
        assertEquals(PrayerNotificationMode.NOTIFICATION_ONLY, mode)
    }

    @Test
    fun sunrise_cyclesOnlyDisabledAndNotification() {
        assertEquals(
            PrayerNotificationMode.NOTIFICATION_ONLY,
            PrayerNotificationModePolicy.nextMode(
                PrayerName.SUNRISE,
                PrayerNotificationMode.DISABLED
            )
        )
        assertEquals(
            PrayerNotificationMode.DISABLED,
            PrayerNotificationModePolicy.nextMode(
                PrayerName.SUNRISE,
                PrayerNotificationMode.NOTIFICATION_ONLY
            )
        )
        assertEquals(
            PrayerNotificationMode.DISABLED,
            PrayerNotificationModePolicy.nextMode(
                PrayerName.SUNRISE,
                PrayerNotificationMode.AZAN
            )
        )
    }

    @Test
    fun prayer_cyclesAllThreeModes() {
        assertEquals(
            PrayerNotificationMode.NOTIFICATION_ONLY,
            PrayerNotificationModePolicy.nextMode(PrayerName.DHUHR, PrayerNotificationMode.DISABLED)
        )
        assertEquals(
            PrayerNotificationMode.AZAN,
            PrayerNotificationModePolicy.nextMode(PrayerName.DHUHR, PrayerNotificationMode.NOTIFICATION_ONLY)
        )
        assertEquals(
            PrayerNotificationMode.DISABLED,
            PrayerNotificationModePolicy.nextMode(PrayerName.DHUHR, PrayerNotificationMode.AZAN)
        )
    }

    @Test
    fun dhuhr_mapsFromZuhrAlias() {
        assertEquals(PrayerName.DHUHR, PrayerName.fromSonicName("Zuhr"))
        assertEquals(PrayerName.DHUHR, PrayerName.fromSonicName("Dhuhr"))
    }
}
