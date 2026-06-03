package com.orbitalsonic.prayertimesample.domain.model

data class LocationInfo(
    val latitude: Double,
    val longitude: Double,
    val address: String = "",
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    val isValid: Boolean get() = latitude != 0.0 || longitude != 0.0

    fun displayAddress(fallback: String): String =
        address.takeIf { it.isNotBlank() } ?: fallback

    fun notificationMessage(): String = address
}
