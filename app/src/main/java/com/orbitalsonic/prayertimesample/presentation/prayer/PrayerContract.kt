package com.orbitalsonic.prayertimesample.presentation.prayer

import com.orbitalsonic.prayertimesample.domain.model.PrayerName

data class PrayerState(
    val isLoading: Boolean = true,
    val locationLabel: String = "",
    val dateLabel: String = "",
    val prayers: List<PrayerUiModel> = emptyList(),
    val nextPrayerName: String = "",
    val countdownText: String = "--:--:--",
    val errorMessage: String? = null
)

sealed interface PrayerIntent {
    data object Load : PrayerIntent
    data object Refresh : PrayerIntent
    data class CycleNotificationMode(val prayer: PrayerName) : PrayerIntent
}

sealed interface PrayerEffect {
    data class ShowMessage(val message: String) : PrayerEffect
    data object RequestLocationPermission : PrayerEffect
}
