package com.orbitalsonic.prayertimesample.domain.usecase

import com.orbitalsonic.prayertimesample.domain.model.NotificationSettings
import com.orbitalsonic.prayertimesample.domain.model.PrayerDayTimes
import com.orbitalsonic.prayertimesample.domain.model.PrayerName
import com.orbitalsonic.prayertimesample.domain.model.PrayerNotificationMode
import com.orbitalsonic.prayertimesample.domain.model.PrayerTimeModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PrayerAlarmPlannerTest {

    @Test
    fun nextAlarms_respectsMaxLimit() {
        val now = 1_000L
        val today = PrayerDayTimes(
            dateMillis = now,
            prayers = PrayerName.entries.mapIndexed { index, name ->
                PrayerTimeModel(
                    name = name,
                    timeLabel = "00:00",
                    timeMillis = now + (index + 1) * 3_600_000L
                )
            }
        )
        val settings = NotificationSettings(globalEnabled = true)
        val alarms = PrayerAlarmPlanner.nextAlarms(now, today, null, settings)
        assertTrue(alarms.size <= PrayerAlarmPlanner.MAX_ACTIVE_ALARMS)
    }

    @Test
    fun nextAlarms_skipsDisabledPrayers() {
        val now = 1_000L
        val fajr = PrayerTimeModel(PrayerName.FAJR, "05:00", now + 10_000)
        val sunrise = PrayerTimeModel(PrayerName.SUNRISE, "06:00", now + 20_000)
        val today = PrayerDayTimes(now, listOf(fajr, sunrise))
        val settings = NotificationSettings(
            globalEnabled = true,
            modes = mapOf(
                PrayerName.FAJR to PrayerNotificationMode.DISABLED,
                PrayerName.SUNRISE to PrayerNotificationMode.NOTIFICATION_ONLY
            )
        )
        val alarms = PrayerAlarmPlanner.nextAlarms(now, today, null, settings, limit = 5)
        assertEquals(1, alarms.size)
        assertEquals(PrayerName.SUNRISE, alarms.first().prayer)
    }
}
