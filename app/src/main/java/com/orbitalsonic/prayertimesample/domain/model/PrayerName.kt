package com.orbitalsonic.prayertimesample.domain.model

import com.orbitalsonic.prayertimesample.R

enum class PrayerName(
    val displayName: String,
    val requestCode: Int,
    val iconRes: Int,
    val prayerType: PrayerType
) {
    FAJR("Fajr", 1001, R.drawable.ic_prayer_fajr, PrayerType.PRAYER),
    SUNRISE("Sunrise", 1002, R.drawable.ic_prayer_sunrise, PrayerType.SUNRISE),
    DHUHR("Dhuhr", 1003, R.drawable.ic_prayer_dhuhr, PrayerType.PRAYER),
    ASR("Asr", 1004, R.drawable.ic_prayer_asr, PrayerType.PRAYER),
    MAGHRIB("Maghrib", 1005, R.drawable.ic_prayer_maghrib, PrayerType.PRAYER),
    ISHA("Isha", 1006, R.drawable.ic_prayer_isha, PrayerType.PRAYER);

    companion object {
        val ordered: List<PrayerName> = entries

        fun fromSonicName(name: String): PrayerName? {
            val normalized = name.trim()
            val alias = when {
                normalized.equals("Zuhr", ignoreCase = true) ||
                    normalized.equals("Zuhur", ignoreCase = true) ||
                    normalized.equals("Zohr", ignoreCase = true) ||
                    normalized.equals("Dhuhr", ignoreCase = true) -> DHUHR
                normalized.equals("Sunset", ignoreCase = true) -> null
                else -> null
            }
            if (alias != null) return alias
            return entries.find {
                it.displayName.equals(normalized, ignoreCase = true) ||
                    it.name.equals(normalized.replace(" ", ""), ignoreCase = true)
            }
        }
    }
}

enum class PrayerType {
    PRAYER,
    SUNRISE
}
