package com.orbitalsonic.prayertimesample.domain.repository

import com.orbitalsonic.prayertimesample.domain.model.PrayerDayTimes
import kotlinx.coroutines.flow.Flow

interface PrayerTimeRepository {
    fun observeTodayPrayerTimes(): Flow<PrayerDayTimes?>
    suspend fun getTodayPrayerTimes(): PrayerDayTimes?
    suspend fun getTomorrowPrayerTimes(): PrayerDayTimes?
    suspend fun refreshPrayerTimes(): PrayerDayTimes?
    suspend fun calculatePrayerTimes(
        latitude: Double,
        longitude: Double,
        dayOffset: Int = 0
    ): PrayerDayTimes?
}
