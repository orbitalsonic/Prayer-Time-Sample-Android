package com.orbitalsonic.prayertimesample.domain.model

data class PrayerTimeModel(
    val name: PrayerName,
    val timeLabel: String,
    val timeMillis: Long,
    val isNext: Boolean = false,
    val isPassed: Boolean = false
)

data class PrayerDayTimes(
    val dateMillis: Long,
    val prayers: List<PrayerTimeModel>
) {
    fun find(name: PrayerName): PrayerTimeModel? = prayers.find { it.name == name }
}
