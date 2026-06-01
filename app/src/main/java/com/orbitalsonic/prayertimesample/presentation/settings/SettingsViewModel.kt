package com.orbitalsonic.prayertimesample.presentation.settings

import com.orbitalsonic.prayertimesample.domain.model.PermissionStatus
import com.orbitalsonic.prayertimesample.domain.repository.PermissionRepository
import com.orbitalsonic.prayertimesample.presentation.common.MviViewModel

class SettingsViewModel(
    private val permissionRepository: PermissionRepository
) : MviViewModel<SettingsIntent, SettingsState, SettingsEffect>(SettingsState()) {

    init {
        refreshPermissions()
    }

    override fun onIntent(intent: SettingsIntent) {
        when (intent) {
            SettingsIntent.RefreshPermissions -> refreshPermissions()
        }
    }

    private fun refreshPermissions() {
        setState {
            copy(
                permissionRows = buildPermissionRows(),
                batteryExempt = permissionRepository.isBatteryOptimizationExempt()
            )
        }
    }

    private fun buildPermissionRows(): List<PermissionRow> = listOf(
        PermissionRow(
            title = "Location",
            status = permissionRepository.locationStatus(),
            actionLabel = "Grant"
        ),
        PermissionRow(
            title = "Notifications",
            status = permissionRepository.notificationStatus(),
            actionLabel = "Grant"
        ),
        PermissionRow(
            title = "Exact alarms",
            status = if (permissionRepository.canScheduleExactAlarms()) {
                PermissionStatus.GRANTED
            } else {
                PermissionStatus.DENIED
            },
            actionLabel = "Settings"
        ),
        PermissionRow(
            title = "Battery optimization",
            status = if (permissionRepository.isBatteryOptimizationExempt()) {
                PermissionStatus.GRANTED
            } else {
                PermissionStatus.DENIED
            },
            actionLabel = "Exempt"
        )
    )
}
