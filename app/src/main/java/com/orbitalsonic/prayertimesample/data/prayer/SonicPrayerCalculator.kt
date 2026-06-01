package com.orbitalsonic.prayertimesample.data.prayer

import com.orbitalsonic.prayertimesample.domain.model.PrayerDayTimes
import com.orbitalsonic.prayertimesample.domain.model.PrayerName
import com.orbitalsonic.prayertimesample.domain.model.PrayerTimeModel
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
        suspendCancellableCoroutine { cont ->
            prayerTimeManager.fetchTodayPrayerTimes(
                latitude = latitude,
                longitude = longitude,
                highLatitudeAdjustment = HighLatitudeAdjustment.NO_ADJUSTMENT,
                asrJuristicMethod = AsrJuristicMethod.HANAFI,
                prayerTimeConvention = PrayerTimeConvention.KARACHI,
                timeFormat = TimeFormat.HOUR_24,
                prayerManualCorrection = PrayerManualCorrection(),
                prayerCustomAngle = PrayerCustomAngle()
            ) { result ->
                if (cont.isActive) {
                    cont.resume(result.getOrNull()?.let { mapPrayerItem(it.date, it.prayerList) })
                }
            }
        }

    private suspend fun fetchSpecificDate(
        latitude: Double,
        longitude: Double,
        date: Date
    ): PrayerDayTimes? = suspendCancellableCoroutine { cont ->
        prayerTimeManager.fetchSpecificDatePrayerTimes(
            latitude = latitude,
            longitude = longitude,
            date,
            highLatitudeAdjustment = HighLatitudeAdjustment.NO_ADJUSTMENT,
            asrJuristicMethod = AsrJuristicMethod.HANAFI,
            prayerTimeConvention = PrayerTimeConvention.KARACHI,
            timeFormat = TimeFormat.HOUR_24,
            prayerManualCorrection = PrayerManualCorrection(),
            prayerCustomAngle = PrayerCustomAngle()
        ) { result ->
            if (cont.isActive) {
                cont.resume(result.getOrNull()?.let { mapPrayerItem(it.date, it.prayerList) })
            }
        }
    }

    private fun mapPrayerItem(
        dateMillis: Long,
        prayerList: List<com.orbitalsonic.sonicopt.models.PrayerTimes>
    ): PrayerDayTimes {
        val now = System.currentTimeMillis()
        val mapped = PrayerName.ordered.map { prayerName ->
            val sonic = prayerList.find {
                PrayerName.fromSonicName(it.prayerName) == prayerName
            }
            if (sonic != null) {
                PrayerTimeModel(
                    name = prayerName,
                    timeLabel = formatDisplayTime(sonic.prayerTimeMillis),
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
        val withNext = mapped.mapIndexed { index, model ->
            model.copy(isNext = index == nextIndex)
        }
        return PrayerDayTimes(dateMillis = dateMillis, prayers = withNext)
    }

    private fun formatDisplayTime(timeMillis: Long): String {
        if (timeMillis <= 0L) return "--:--"
        return displayTimeFormat.format(Date(timeMillis)).uppercase(Locale.getDefault())
    }
}
