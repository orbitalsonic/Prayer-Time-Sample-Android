package com.orbitalsonic.prayertimesample.presentation.prayer

import com.orbitalsonic.prayertimesample.R
import com.orbitalsonic.prayertimesample.domain.model.PrayerName
import com.orbitalsonic.prayertimesample.domain.model.PrayerNotificationMode
import com.orbitalsonic.prayertimesample.domain.model.PrayerTimeModel
import com.orbitalsonic.prayertimesample.domain.model.PrayerType

enum class NotificationMode {
    DISABLED,
    NOTIFICATION_ONLY,
    AZAN;

    companion object {
        fun fromDomain(mode: PrayerNotificationMode): NotificationMode = when (mode) {
            PrayerNotificationMode.DISABLED -> DISABLED
            PrayerNotificationMode.NOTIFICATION_ONLY -> NOTIFICATION_ONLY
            PrayerNotificationMode.AZAN -> AZAN
        }

        fun toDomain(mode: NotificationMode): PrayerNotificationMode = when (mode) {
            DISABLED -> PrayerNotificationMode.DISABLED
            NOTIFICATION_ONLY -> PrayerNotificationMode.NOTIFICATION_ONLY
            AZAN -> PrayerNotificationMode.AZAN
        }
    }

    fun iconRes(): Int = when (this) {
        DISABLED -> R.drawable.ic_notification_off
        NOTIFICATION_ONLY -> R.drawable.ic_notification
        AZAN -> R.drawable.ic_volume_up
    }
}

data class PrayerUiModel(
    val prayerName: String,
    val prayerTime: String,
    val prayerType: PrayerType,
    val notificationMode: NotificationMode,
    val prayer: PrayerName,
    val iconRes: Int,
    val isNext: Boolean = false,
    val isPassed: Boolean = false
)

fun buildPrayerUiModels(
    times: List<PrayerTimeModel>,
    modes: Map<PrayerName, PrayerNotificationMode>
): List<PrayerUiModel> {
    return PrayerName.ordered.map { prayer ->
        val time = times.find { it.name == prayer }
        val mode = NotificationMode.fromDomain(
            modes[prayer] ?: PrayerNotificationMode.DISABLED
        )
        PrayerUiModel(
            prayerName = prayer.displayName,
            prayerTime = time?.timeLabel ?: "--:--",
            prayerType = prayer.prayerType,
            notificationMode = mode,
            prayer = prayer,
            iconRes = prayer.iconRes,
            isNext = time?.isNext == true,
            isPassed = time?.isPassed == true
        )
    }
}
