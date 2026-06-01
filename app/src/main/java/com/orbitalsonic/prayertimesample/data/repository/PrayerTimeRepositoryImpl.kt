package com.orbitalsonic.prayertimesample.data.repository

import com.orbitalsonic.prayertimesample.data.local.PrayerPreferencesDataStore
import com.orbitalsonic.prayertimesample.data.prayer.SonicPrayerCalculator
import com.orbitalsonic.prayertimesample.domain.model.PrayerDayTimes
import com.orbitalsonic.prayertimesample.domain.repository.LocationRepository
import com.orbitalsonic.prayertimesample.domain.repository.PrayerTimeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PrayerTimeRepositoryImpl(
    private val calculator: SonicPrayerCalculator,
    private val locationRepository: LocationRepository,
    private val dataStore: PrayerPreferencesDataStore
) : PrayerTimeRepository {

    private val mutex = Mutex()
    private val todayFlow = MutableStateFlow<PrayerDayTimes?>(null)
    private var tomorrowCache: PrayerDayTimes? = null

    override fun observeTodayPrayerTimes(): Flow<PrayerDayTimes?> = todayFlow.asStateFlow()

    override suspend fun getTodayPrayerTimes(): PrayerDayTimes? =
        todayFlow.value ?: refreshPrayerTimes()

    override suspend fun getTomorrowPrayerTimes(): PrayerDayTimes? {
        tomorrowCache?.let { return it }
        val location = locationRepository.getCachedLocation() ?: return null
        return calculatePrayerTimes(location.latitude, location.longitude, dayOffset = 1)
            ?.also { tomorrowCache = it }
    }

    override suspend fun refreshPrayerTimes(): PrayerDayTimes? = mutex.withLock {
        val location = locationRepository.getCachedLocation()
            ?: locationRepository.refreshLocation()
        if (location == null || !location.isValid) return null
        val today = calculatePrayerTimes(location.latitude, location.longitude, 0)
        todayFlow.value = today
        tomorrowCache = calculatePrayerTimes(location.latitude, location.longitude, 1)
        today
    }

    override suspend fun calculatePrayerTimes(
        latitude: Double,
        longitude: Double,
        dayOffset: Int
    ): PrayerDayTimes? = calculator.calculate(latitude, longitude, dayOffset)
}
