package com.orbitalsonic.prayertimesample.presentation.location

import com.orbitalsonic.prayertimesample.domain.model.PermissionStatus

data class LocationState(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "",
    val isLoading: Boolean = false,
    val permissionStatus: PermissionStatus = PermissionStatus.DENIED
)

sealed interface LocationIntent {
    data object Refresh : LocationIntent
    data object LoadCached : LocationIntent
}

sealed interface LocationEffect {
    data object RequestPermission : LocationEffect
    data object OpenAppSettings : LocationEffect
    data class ShowError(val message: String) : LocationEffect
}
