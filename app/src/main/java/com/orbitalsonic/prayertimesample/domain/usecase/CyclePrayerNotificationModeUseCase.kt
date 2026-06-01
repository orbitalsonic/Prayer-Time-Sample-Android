package com.orbitalsonic.prayertimesample.domain.usecase

import com.orbitalsonic.prayertimesample.domain.model.PrayerName
import com.orbitalsonic.prayertimesample.domain.model.PrayerNotificationMode
import com.orbitalsonic.prayertimesample.domain.model.PrayerNotificationModePolicy
import com.orbitalsonic.prayertimesample.domain.repository.NotificationSettingsRepository

class CyclePrayerNotificationModeUseCase(
    private val settingsRepository: NotificationSettingsRepository,
    private val schedulePrayerAlarmsUseCase: SchedulePrayerAlarmsUseCase
) {
    suspend operator fun invoke(prayer: PrayerName): PrayerNotificationMode {
        val settings = settingsRepository.getSettings()
        val current = settings.modeFor(prayer)
        val next = PrayerNotificationModePolicy.nextMode(prayer, current)
        settingsRepository.setPrayerMode(prayer, next)
        schedulePrayerAlarmsUseCase()
        return next
    }
}
