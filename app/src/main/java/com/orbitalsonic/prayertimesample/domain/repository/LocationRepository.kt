package com.orbitalsonic.prayertimesample.domain.repository

import com.orbitalsonic.prayertimesample.domain.model.LocationInfo
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    fun observeLocation(): Flow<LocationInfo?>
    suspend fun getCachedLocation(): LocationInfo?
    suspend fun refreshLocation(): LocationInfo?
    suspend fun saveLocation(location: LocationInfo)
}
