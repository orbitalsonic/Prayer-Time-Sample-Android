package com.orbitalsonic.prayertimesample.data.repository

import com.orbitalsonic.prayertimesample.data.local.PrayerPreferencesDataStore
import com.orbitalsonic.prayertimesample.data.location.AddressResolver
import com.orbitalsonic.prayertimesample.data.location.FusedLocationDataSource
import com.orbitalsonic.prayertimesample.domain.model.LocationInfo
import com.orbitalsonic.prayertimesample.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class LocationRepositoryImpl(
    private val fusedLocationDataSource: FusedLocationDataSource,
    private val dataStore: PrayerPreferencesDataStore,
    private val addressResolver: AddressResolver
) : LocationRepository {

    private val locationFlow = MutableStateFlow<LocationInfo?>(null)

    override fun observeLocation(): Flow<LocationInfo?> = locationFlow.asStateFlow()

    override suspend fun getCachedLocation(): LocationInfo? {
        locationFlow.value?.let { return it }
        val cached = dataStore.observeCachedLocation().map { triple ->
            triple?.let { (lat, lng, address) ->
                LocationInfo(lat, lng, address)
            }
        }.first()
        val withAddress = cached?.let { ensureAddress(it) }
        locationFlow.value = withAddress
        return withAddress
    }

    override suspend fun refreshLocation(): LocationInfo? {
        val fresh = fusedLocationDataSource.getCurrentLocation()
            ?: fusedLocationDataSource.getLastKnown()?.let { loc ->
                LocationInfo(loc.latitude, loc.longitude)
            }
        val resolved = fresh?.let { ensureAddress(it) }
        resolved?.let { saveLocation(it) }
        return resolved
    }

    override suspend fun saveLocation(location: LocationInfo) {
        val withAddress = ensureAddress(location)
        dataStore.saveLocation(
            withAddress.latitude,
            withAddress.longitude,
            withAddress.address
        )
        locationFlow.value = withAddress
    }

    private suspend fun ensureAddress(location: LocationInfo): LocationInfo {
        if (location.address.isNotBlank() || !location.isValid) return location
        val address = addressResolver.resolve(location.latitude, location.longitude).orEmpty()
        return location.copy(address = address)
    }
}
