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

    suspend fun resolve(latitude: Double, longitude: Double): String? = withContext(Dispatchers.IO) {
        if (!Geocoder.isPresent()) return@withContext null
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { cont ->
                    geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                        cont.resume(addresses.firstOrNull()?.toDisplayAddress())
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(latitude, longitude, 1)
                    ?.firstOrNull()
                    ?.toDisplayAddress()
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun android.location.Address.toDisplayAddress(): String {
        val line = getAddressLine(0)
        if (!line.isNullOrBlank()) return line
        val city = locality ?: subAdminArea
        val region = adminArea
        val country = countryName
        return listOfNotNull(city, region, country)
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(", ")
            .ifBlank { "" }
    }
}
