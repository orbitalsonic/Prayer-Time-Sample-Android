package com.orbitalsonic.prayertimesample.domain.usecase

import com.orbitalsonic.prayertimesample.domain.model.NotificationSettings
import com.orbitalsonic.prayertimesample.domain.repository.NotificationSettingsRepository
import kotlinx.coroutines.flow.Flow

class ObserveNotificationSettingsUseCase(
    private val settingsRepository: NotificationSettingsRepository
) {
    operator fun invoke(): Flow<NotificationSettings> = settingsRepository.observeSettings()
}
