package com.orbitalsonic.prayertimesample.presentation.settings.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.orbitalsonic.prayertimesample.domain.model.PermissionStatus

enum class PermissionType {
    LOCATION,
    NOTIFICATION,
    EXACT_ALARM,
    BATTERY_OPTIMIZATION
}

data class PermissionItem(
    val type: PermissionType,
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    @DrawableRes val iconRes: Int,
    val isGranted: Boolean,
    val status: PermissionStatus = PermissionStatus.DENIED
)
