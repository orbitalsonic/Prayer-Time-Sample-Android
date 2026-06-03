package com.orbitalsonic.prayertimesample.presentation.settings

import com.orbitalsonic.sonicopt.enums.PrayerTimeConvention

data class ConventionItem(
    val convention: PrayerTimeConvention,
    val title: String,
    val subtitle: String,
    val isSelected: Boolean = false
)
