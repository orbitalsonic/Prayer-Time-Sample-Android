package com.orbitalsonic.prayertimesample.data.prayer

import com.orbitalsonic.prayertimesample.domain.model.PrayerDayTimes
import com.orbitalsonic.prayertimesample.domain.model.PrayerName
import com.orbitalsonic.prayertimesample.domain.model.PrayerTimeModel
import com.orbitalsonic.prayertimesample.data.local.PrayerPreferencesDataStore
import com.orbitalsonic.sonicopt.enums.AsrJuristicMethod
import com.orbitalsonic.sonicopt.enums.HighLatitudeAdjustment
import com.orbitalsonic.sonicopt.enums.PrayerTimeConvention
import com.orbitalsonic.sonicopt.enums.TimeFormat
import com.orbitalsonic.sonicopt.manager.PrayerTimeManager
import com.orbitalsonic.sonicopt.models.PrayerCustomAngle
import com.orbitalsonic.sonicopt.models.PrayerManualCorrection
import kotlinx.coroutines.suspendCancellableCoroutine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume

class SonicPrayerCalculator(
    private val prayerPreferencesDataStore: PrayerPreferencesDataStore,
    private val prayerTimeManager: PrayerTimeManager = PrayerTimeManager()
) {

    private val displayTimeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    suspend fun calculate(
        latitude: Double,
        longitude: Double,
        dayOffset: Int = 0
    ): PrayerDayTimes? {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, dayOffset)
        }
        val targetDate = calendar.time

        return if (dayOffset == 0) {
            fetchToday(latitude, longitude)
        } else {
            fetchSpecificDate(latitude, longitude, targetDate)
        }
    }

    private suspend fun fetchToday(latitude: Double, longitude: Double): PrayerDayTimes? =
        run {
            val settings = loadRuntimeSettings()
            suspendCancellableCoroutine { cont ->
            prayerTimeManager.fetchTodayPrayerTimes(
                latitude = latitude,
                longitude = longitude,
                highLatitudeAdjustment = settings.highLatitudeAdjustment,
                asrJuristicMethod = settings.asrJuristicMethod,
                prayerTimeConvention = settings.prayerTimeConvention,
                timeFormat = settings.timeFormat,
                prayerManualCorrection = settings.prayerManualCorrection,
                prayerCustomAngle = settings.prayerCustomAngle
            ) { result ->
                if (cont.isActive) {
                    cont.resume(
                        result.getOrNull()?.let {
                            mapPrayerItem(it.date, it.prayerList, settings.timeFormat)
                        }
                    )
                }
            }
        }
        }

    private suspend fun fetchSpecificDate(
        latitude: Double,
        longitude: Double,
        date: Date
    ): PrayerDayTimes? {
        val settings = loadRuntimeSettings()
        return suspendCancellableCoroutine { cont ->
            prayerTimeManager.fetchSpecificDatePrayerTimes(
                latitude = latitude,
                longitude = longitude,
                date,
                highLatitudeAdjustment = settings.highLatitudeAdjustment,
                asrJuristicMethod = settings.asrJuristicMethod,
                prayerTimeConvention = settings.prayerTimeConvention,
                timeFormat = settings.timeFormat,
                prayerManualCorrection = settings.prayerManualCorrection,
                prayerCustomAngle = settings.prayerCustomAngle
            ) { result ->
                if (cont.isActive) {
                    cont.resume(
                        result.getOrNull()?.let {
                            mapPrayerItem(it.date, it.prayerList, settings.timeFormat)
                        }
                    )
                }
            }
        }
    }

    private fun mapPrayerItem(
        dateMillis: Long,
        prayerList: List<com.orbitalsonic.sonicopt.models.PrayerTimes>,
        timeFormat: TimeFormat
    ): PrayerDayTimes {
        val now = System.currentTimeMillis()
        val mapped = PrayerName.ordered.map { prayerName ->
            val sonic = prayerList.find {
                PrayerName.fromSonicName(it.prayerName) == prayerName
            }
            if (sonic != null) {
                PrayerTimeModel(
                    name = prayerName,
                    timeLabel = formatPrayerTimeLabel(sonic.prayerTime, timeFormat, sonic.prayerTimeMillis),
                    timeMillis = sonic.prayerTimeMillis,
                    isPassed = sonic.prayerTimeMillis <= now
                )
            } else {
                PrayerTimeModel(
                    name = prayerName,
                    timeLabel = "--:--",
                    timeMillis = 0L,
                    isPassed = false
                )
            }
        }
        val nextIndex = mapped.indexOfFirst { !it.isPassed && it.timeMillis > 0 }.takeIf { it >= 0 }
        val nowIndex = findCurrentPrayerIndex(mapped, now)
        val withNext = mapped.mapIndexed { index, model ->
            model.copy(
                isNext = index == nextIndex,
                isNow = index == nowIndex
            )
        }
        return PrayerDayTimes(dateMillis = dateMillis, prayers = withNext)
    }

    private fun findCurrentPrayerIndex(
        prayers: List<PrayerTimeModel>,
        nowMillis: Long
    ): Int? {
        val validIndices = prayers.indices.filter { prayers[it].timeMillis > 0L }
        if (validIndices.isEmpty()) return null
        for (pos in validIndices.indices) {
            val currentIndex = validIndices[pos]
            val current = prayers[currentIndex]
            val nextIndex = validIndices.getOrNull(pos + 1)
            val nextTime = nextIndex?.let { prayers[it].timeMillis } ?: Long.MAX_VALUE
            if (nowMillis in current.timeMillis until nextTime) {
                return currentIndex
            }
        }
        return validIndices.lastOrNull()
    }

    private fun formatPrayerTimeLabel(
        libraryTime: String,
        timeFormat: TimeFormat,
        timeMillis: Long
    ): String {
        if (timeMillis <= 0L) return "--:--"
        if (libraryTime.isNotBlank()) {
            return if (timeFormat == TimeFormat.HOUR_12) {
                libraryTime.uppercase(Locale.getDefault())
            } else {
                libraryTime
            }
        }
        return formatDisplayTime(timeMillis, timeFormat)
    }

    private fun formatDisplayTime(timeMillis: Long, timeFormat: TimeFormat): String {
        if (timeMillis <= 0L) return "--:--"
        return when (timeFormat) {
            TimeFormat.HOUR_24 -> SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(Date(timeMillis))
            TimeFormat.HOUR_12_NS -> SimpleDateFormat("hh:mm", Locale.getDefault())
                .format(Date(timeMillis))
            TimeFormat.FLOATING -> {
                val calendar = Calendar.getInstance().apply { timeInMillis = timeMillis }
                val hours = calendar.get(Calendar.HOUR_OF_DAY)
                val minutes = calendar.get(Calendar.MINUTE)
                "${hours + minutes / 60.0}"
            }
            else -> displayTimeFormat.format(Date(timeMillis)).uppercase(Locale.getDefault())
        }
    }

    private data class PrayerRuntimeSettings(
        val highLatitudeAdjustment: HighLatitudeAdjustment,
        val asrJuristicMethod: AsrJuristicMethod,
        val prayerTimeConvention: PrayerTimeConvention,
        val timeFormat: TimeFormat,
        val prayerManualCorrection: PrayerManualCorrection,
        val prayerCustomAngle: PrayerCustomAngle
    )

    private suspend fun loadRuntimeSettings(): PrayerRuntimeSettings =
        PrayerRuntimeSettings(
            highLatitudeAdjustment = prayerPreferencesDataStore.getHighLatitudeAdjustment(),
            asrJuristicMethod = prayerPreferencesDataStore.getAsrJuristicMethod(),
            prayerTimeConvention = prayerPreferencesDataStore.getPrayerTimeConvention(),
            timeFormat = prayerPreferencesDataStore.getTimeFormat(),
            prayerManualCorrection = prayerPreferencesDataStore.getPrayerManualCorrection(),
            prayerCustomAngle = prayerPreferencesDataStore.getPrayerCustomAngle()
        )
}
