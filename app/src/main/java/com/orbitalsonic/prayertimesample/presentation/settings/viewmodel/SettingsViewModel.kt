package com.orbitalsonic.prayertimesample.presentation.settings.viewmodel

import com.orbitalsonic.prayertimesample.presentation.common.MviViewModel
import com.orbitalsonic.prayertimesample.presentation.settings.contract.SettingsEffect
import com.orbitalsonic.prayertimesample.presentation.settings.contract.SettingsIntent
import com.orbitalsonic.prayertimesample.presentation.settings.contract.SettingsState
import com.orbitalsonic.prayertimesample.presentation.settings.permission.PermissionStateChecker

class SettingsViewModel(
    private val permissionStateChecker: PermissionStateChecker
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
            copy(permissionItems = permissionStateChecker.buildPermissionItems())
        }
    }
}
