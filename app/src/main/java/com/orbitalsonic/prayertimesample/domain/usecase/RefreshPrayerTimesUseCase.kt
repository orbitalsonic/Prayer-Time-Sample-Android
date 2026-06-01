package com.orbitalsonic.prayertimesample.domain.usecase

import com.orbitalsonic.prayertimesample.domain.model.PrayerDayTimes
import com.orbitalsonic.prayertimesample.domain.repository.LocationRepository
import com.orbitalsonic.prayertimesample.domain.repository.PrayerTimeRepository

class RefreshPrayerTimesUseCase(
    private val prayerTimeRepository: PrayerTimeRepository,
    private val locationRepository: LocationRepository,
    private val schedulePrayerAlarmsUseCase: SchedulePrayerAlarmsUseCase
) {
    suspend operator fun invoke(refreshLocation: Boolean = false): PrayerDayTimes? {
        if (refreshLocation) {
            locationRepository.refreshLocation()
        }
        val times = prayerTimeRepository.refreshPrayerTimes()
        schedulePrayerAlarmsUseCase()
        return times
    }
}
