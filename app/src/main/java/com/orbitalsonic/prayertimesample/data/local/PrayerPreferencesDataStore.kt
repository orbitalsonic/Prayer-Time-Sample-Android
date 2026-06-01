package com.orbitalsonic.prayertimesample.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.orbitalsonic.prayertimesample.domain.model.NotificationSettings
import com.orbitalsonic.prayertimesample.domain.model.PrayerName
import com.orbitalsonic.prayertimesample.domain.model.PrayerNotificationMode
import com.orbitalsonic.prayertimesample.domain.model.PrayerNotificationModePolicy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.prayerDataStore: DataStore<Preferences> by preferencesDataStore("prayer_settings")

class PrayerPreferencesDataStore(private val context: Context) {

    private object Keys {
        val GLOBAL_ENABLED = booleanPreferencesKey("global_notifications_enabled")
        fun prayerModeKey(prayer: PrayerName) =
            intPreferencesKey("prayer_mode_${prayer.name.lowercase()}")
        val LATITUDE = stringPreferencesKey("cached_latitude")
        val LONGITUDE = stringPreferencesKey("cached_longitude")
        val ADDRESS = stringPreferencesKey("cached_address")
        val LOCATION_UPDATED = stringPreferencesKey("location_updated_at")
    }

    fun observeNotificationSettings(): Flow<NotificationSettings> =
        context.prayerDataStore.data.map { prefs ->
            NotificationSettings(
                globalEnabled = prefs[Keys.GLOBAL_ENABLED] ?: true,
                modes = PrayerName.ordered.associateWith { prayer ->
                    val stored = PrayerNotificationMode.fromOrdinal(
                        prefs[Keys.prayerModeKey(prayer)] ?: PrayerNotificationMode.NOTIFICATION_ONLY.ordinal
                    )
                    PrayerNotificationModePolicy.effectiveMode(prayer, stored)
                }
            )
        }

    suspend fun getNotificationSettings(): NotificationSettings =
        observeNotificationSettings().first()

    suspend fun setGlobalEnabled(enabled: Boolean) {
        context.prayerDataStore.edit { it[Keys.GLOBAL_ENABLED] = enabled }
    }

    suspend fun setPrayerMode(prayer: PrayerName, mode: PrayerNotificationMode) {
        context.prayerDataStore.edit { it[Keys.prayerModeKey(prayer)] = mode.ordinal }
    }

    fun observeCachedLocation(): Flow<Triple<Double, Double, String>?> =
        context.prayerDataStore.data.map { prefs ->
            val lat = prefs[Keys.LATITUDE]?.toDoubleOrNull()
            val lng = prefs[Keys.LONGITUDE]?.toDoubleOrNull()
            if (lat != null && lng != null) {
                Triple(lat, lng, prefs[Keys.ADDRESS].orEmpty())
            } else {
                null
            }
        }

    suspend fun saveLocation(latitude: Double, longitude: Double, address: String) {
        context.prayerDataStore.edit {
            it[Keys.LATITUDE] = latitude.toString()
            it[Keys.LONGITUDE] = longitude.toString()
            it[Keys.ADDRESS] = address
            it[Keys.LOCATION_UPDATED] = System.currentTimeMillis().toString()
        }
    }

    suspend fun getLocationUpdatedAt(): Long =
        context.prayerDataStore.data.map {
            it[Keys.LOCATION_UPDATED]?.toLongOrNull() ?: 0L
        }.first()
}
