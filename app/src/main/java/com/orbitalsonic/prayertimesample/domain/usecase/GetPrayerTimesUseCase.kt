package com.orbitalsonic.prayertimesample.domain.usecase

import com.orbitalsonic.prayertimesample.domain.model.PrayerDayTimes
import com.orbitalsonic.prayertimesample.domain.repository.PrayerTimeRepository
import kotlinx.coroutines.flow.Flow

class GetPrayerTimesUseCase(
    private val prayerTimeRepository: PrayerTimeRepository
) {
    operator fun invoke(): Flow<PrayerDayTimes?> = prayerTimeRepository.observeTodayPrayerTimes()

    suspend fun today(): PrayerDayTimes? = prayerTimeRepository.getTodayPrayerTimes()

    suspend fun tomorrow(): PrayerDayTimes? = prayerTimeRepository.getTomorrowPrayerTimes()
}
