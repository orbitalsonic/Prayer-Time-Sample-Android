package com.orbitalsonic.prayertimesample.domain.repository

import com.orbitalsonic.prayertimesample.domain.model.NotificationSettings
import com.orbitalsonic.prayertimesample.domain.model.PrayerName
import com.orbitalsonic.prayertimesample.domain.model.PrayerNotificationMode
import kotlinx.coroutines.flow.Flow

interface NotificationSettingsRepository {
    fun observeSettings(): Flow<NotificationSettings>
    suspend fun getSettings(): NotificationSettings
    suspend fun setGlobalEnabled(enabled: Boolean)
    suspend fun setPrayerMode(prayer: PrayerName, mode: PrayerNotificationMode)
}
