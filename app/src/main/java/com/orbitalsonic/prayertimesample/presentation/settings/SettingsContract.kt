package com.orbitalsonic.prayertimesample.presentation.settings

import com.orbitalsonic.prayertimesample.domain.model.PermissionStatus

data class PermissionRow(
    val title: String,
    val status: PermissionStatus,
    val actionLabel: String
)

data class SettingsState(
    val permissionRows: List<PermissionRow> = emptyList(),
    val batteryExempt: Boolean = false
)

sealed interface SettingsIntent {
    data object RefreshPermissions : SettingsIntent
}

sealed interface SettingsEffect {
    data object OpenExactAlarmSettings : SettingsEffect
    data object OpenBatteryOptimization : SettingsEffect
}
