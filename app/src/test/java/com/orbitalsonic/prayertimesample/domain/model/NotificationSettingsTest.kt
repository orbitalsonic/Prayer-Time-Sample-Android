package com.orbitalsonic.prayertimesample.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class NotificationSettingsTest {

    @Test
    fun modeFor_returnsDisabled_whenGlobalOff() {
        val settings = NotificationSettings(
            globalEnabled = false,
            modes = mapOf(PrayerName.FAJR to PrayerNotificationMode.AZAN)
        )
        assertEquals(PrayerNotificationMode.DISABLED, settings.modeFor(PrayerName.FAJR))
    }

    @Test
    fun isActive_false_whenModeDisabled() {
        val settings = NotificationSettings(
            globalEnabled = true,
            modes = mapOf(PrayerName.ISHA to PrayerNotificationMode.DISABLED)
        )
        assertFalse(settings.isActive(PrayerName.ISHA))
    }
}
