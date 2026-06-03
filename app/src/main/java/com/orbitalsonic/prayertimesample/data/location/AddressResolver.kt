package com.orbitalsonic.prayertimesample.data.location

import android.content.Context
import android.location.Geocoder
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

class AddressResolver(context: Context) {

    private val geocoder = Geocoder(context.applicationContext, Locale.getDefault())

    suspend fun resolve(latitude: Double, longitude: Double): String? =
        resolveAddress(latitude, longitude)?.toDisplayAddress()

    private suspend fun resolveAddress(
        latitude: Double,
        longitude: Double
    ): android.location.Address? = withContext(Dispatchers.IO) {
        if (!Geocoder.isPresent()) return@withContext null
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { cont ->
                    geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                        cont.resume(addresses.firstOrNull())
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun android.location.Address.toDisplayAddress(): String {
        val city = locality ?: subLocality ?: subAdminArea
        val country = countryName
        return listOfNotNull(city, country)
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(", ")
    }
}
