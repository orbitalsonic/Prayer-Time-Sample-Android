package com.orbitalsonic.prayertimesample.data.repository

import com.orbitalsonic.prayertimesample.data.local.PrayerPreferencesDataStore
import com.orbitalsonic.prayertimesample.domain.model.NotificationSettings
import com.orbitalsonic.prayertimesample.domain.model.PrayerName
import com.orbitalsonic.prayertimesample.domain.model.PrayerNotificationMode
import com.orbitalsonic.prayertimesample.domain.model.PrayerNotificationModePolicy
import com.orbitalsonic.prayertimesample.domain.repository.NotificationSettingsRepository
import kotlinx.coroutines.flow.Flow

class NotificationSettingsRepositoryImpl(
    private val dataStore: PrayerPreferencesDataStore
) : NotificationSettingsRepository {

    override fun observeSettings(): Flow<NotificationSettings> =
        dataStore.observeNotificationSettings()

    override suspend fun getSettings(): NotificationSettings =
        dataStore.getNotificationSettings()

    override suspend fun setGlobalEnabled(enabled: Boolean) {
        dataStore.setGlobalEnabled(enabled)
    }

    override suspend fun setPrayerMode(prayer: PrayerName, mode: PrayerNotificationMode) {
        val normalized = PrayerNotificationModePolicy.effectiveMode(prayer, mode)
        dataStore.setPrayerMode(prayer, normalized)
    }

}
