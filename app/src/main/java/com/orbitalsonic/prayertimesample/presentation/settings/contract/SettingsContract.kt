package com.orbitalsonic.prayertimesample.presentation.settings.contract

import com.orbitalsonic.prayertimesample.presentation.settings.model.PermissionItem

data class SettingsState(
    val permissionItems: List<PermissionItem> = emptyList()
)

sealed interface SettingsIntent {
    data object RefreshPermissions : SettingsIntent
}

sealed interface SettingsEffect {
    data object OpenExactAlarmSettings : SettingsEffect
    data object OpenBatteryOptimization : SettingsEffect
}
