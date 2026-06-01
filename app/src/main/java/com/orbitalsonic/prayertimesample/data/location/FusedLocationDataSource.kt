package com.orbitalsonic.prayertimesample.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.orbitalsonic.prayertimesample.domain.model.LocationInfo
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class FusedLocationDataSource(
    context: Context,
    private val addressResolver: AddressResolver = AddressResolver(context)
) {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LocationInfo? {
        val location = requestCurrent() ?: getLastKnown()
        return location?.toLocationInfo()
    }

    @SuppressLint("MissingPermission")
    suspend fun getLastKnown(): Location? = suspendCancellableCoroutine { cont ->
        fusedClient.lastLocation
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { cont.resume(null) }
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestCurrent(): Location? = suspendCancellableCoroutine { cont ->
        val token = CancellationTokenSource()
        fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, token.token)
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { cont.resume(null) }
    }

    private suspend fun Location.toLocationInfo(): LocationInfo {
        val resolvedAddress = addressResolver.resolve(latitude, longitude).orEmpty()
        return LocationInfo(
            latitude = latitude,
            longitude = longitude,
            address = resolvedAddress,
            updatedAtMillis = System.currentTimeMillis()
        )
    }
}
