package com.orbitalsonic.prayertimesample.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.orbitalsonic.sonicopt.enums.AsrJuristicMethod
import com.orbitalsonic.sonicopt.enums.HighLatitudeAdjustment
import com.orbitalsonic.sonicopt.enums.PrayerTimeConvention
import com.orbitalsonic.sonicopt.enums.TimeFormat
import com.orbitalsonic.sonicopt.models.PrayerCustomAngle
import com.orbitalsonic.sonicopt.models.PrayerManualCorrection
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
        val PRAYER_TIME_CONVENTION = stringPreferencesKey("prayer_time_convention")
        val TIME_FORMAT = stringPreferencesKey("time_format")
        val ASR_JURISTIC_METHOD = stringPreferencesKey("asr_juristic_method")
        val HIGH_LATITUDE_ADJUSTMENT = stringPreferencesKey("high_latitude_adjustment")
        val CUSTOM_FAJR_ANGLE = stringPreferencesKey("custom_fajr_angle")
        val CUSTOM_ISHA_ANGLE = stringPreferencesKey("custom_isha_angle")
        val FAJR_CORRECTION_MINUTES = intPreferencesKey("fajr_correction_minutes")
        val DHUHR_CORRECTION_MINUTES = intPreferencesKey("dhuhr_correction_minutes")
        val ASR_CORRECTION_MINUTES = intPreferencesKey("asr_correction_minutes")
        val MAGHRIB_CORRECTION_MINUTES = intPreferencesKey("maghrib_correction_minutes")
        val ISHA_CORRECTION_MINUTES = intPreferencesKey("isha_correction_minutes")
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

    suspend fun getPrayerTimeConvention(): PrayerTimeConvention =
        context.prayerDataStore.data.map { prefs ->
            PrayerTimeConvention.entries.find { it.name == prefs[Keys.PRAYER_TIME_CONVENTION] }
                ?: PrayerTimeConvention.KARACHI
        }.first()

    suspend fun setPrayerTimeConvention(value: PrayerTimeConvention) {
        context.prayerDataStore.edit { it[Keys.PRAYER_TIME_CONVENTION] = value.name }
    }

    suspend fun getTimeFormat(): TimeFormat =
        context.prayerDataStore.data.map { prefs ->
            TimeFormat.entries.find { it.name == prefs[Keys.TIME_FORMAT] } ?: TimeFormat.HOUR_12
        }.first()

    suspend fun setTimeFormat(value: TimeFormat) {
        context.prayerDataStore.edit { it[Keys.TIME_FORMAT] = value.name }
    }

    suspend fun getAsrJuristicMethod(): AsrJuristicMethod =
        context.prayerDataStore.data.map { prefs ->
            AsrJuristicMethod.entries.find { it.name == prefs[Keys.ASR_JURISTIC_METHOD] }
                ?: AsrJuristicMethod.HANAFI
        }.first()

    suspend fun setAsrJuristicMethod(value: AsrJuristicMethod) {
        context.prayerDataStore.edit { it[Keys.ASR_JURISTIC_METHOD] = value.name }
    }

    suspend fun getHighLatitudeAdjustment(): HighLatitudeAdjustment =
        context.prayerDataStore.data.map { prefs ->
            HighLatitudeAdjustment.entries.find { it.name == prefs[Keys.HIGH_LATITUDE_ADJUSTMENT] }
                ?: HighLatitudeAdjustment.NO_ADJUSTMENT
        }.first()

    suspend fun setHighLatitudeAdjustment(value: HighLatitudeAdjustment) {
        context.prayerDataStore.edit { it[Keys.HIGH_LATITUDE_ADJUSTMENT] = value.name }
    }

    suspend fun getPrayerCustomAngle(): PrayerCustomAngle =
        context.prayerDataStore.data.map { prefs ->
            PrayerCustomAngle(
                fajrAngle = prefs[Keys.CUSTOM_FAJR_ANGLE]?.toDoubleOrNull() ?: 9.0,
                ishaAngle = prefs[Keys.CUSTOM_ISHA_ANGLE]?.toDoubleOrNull() ?: 14.0
            )
        }.first()

    suspend fun setPrayerCustomAngle(fajr: Double, isha: Double) {
        context.prayerDataStore.edit {
            it[Keys.CUSTOM_FAJR_ANGLE] = fajr.toString()
            it[Keys.CUSTOM_ISHA_ANGLE] = isha.toString()
        }
    }

    suspend fun getPrayerManualCorrection(): PrayerManualCorrection =
        context.prayerDataStore.data.map { prefs ->
            PrayerManualCorrection(
                fajrMinute = prefs[Keys.FAJR_CORRECTION_MINUTES] ?: 0,
                zuhrMinute = prefs[Keys.DHUHR_CORRECTION_MINUTES] ?: 0,
                asrMinute = prefs[Keys.ASR_CORRECTION_MINUTES] ?: 0,
                maghribMinute = prefs[Keys.MAGHRIB_CORRECTION_MINUTES] ?: 0,
                ishaMinute = prefs[Keys.ISHA_CORRECTION_MINUTES] ?: 0
            )
        }.first()

    suspend fun setFajrCorrectionMinutes(value: Int) {
        context.prayerDataStore.edit { it[Keys.FAJR_CORRECTION_MINUTES] = value }
    }

    suspend fun setDhuhrCorrectionMinutes(value: Int) {
        context.prayerDataStore.edit { it[Keys.DHUHR_CORRECTION_MINUTES] = value }
    }

    suspend fun setAsrCorrectionMinutes(value: Int) {
        context.prayerDataStore.edit { it[Keys.ASR_CORRECTION_MINUTES] = value }
    }

    suspend fun setMaghribCorrectionMinutes(value: Int) {
        context.prayerDataStore.edit { it[Keys.MAGHRIB_CORRECTION_MINUTES] = value }
    }

    suspend fun setIshaCorrectionMinutes(value: Int) {
        context.prayerDataStore.edit { it[Keys.ISHA_CORRECTION_MINUTES] = value }
    }
}
