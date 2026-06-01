package com.orbitalsonic.prayertimesample.domain.usecase

import com.orbitalsonic.prayertimesample.domain.model.LocationInfo
import com.orbitalsonic.prayertimesample.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow

class ObserveLocationUseCase(
    private val locationRepository: LocationRepository
) {
    operator fun invoke(): Flow<LocationInfo?> = locationRepository.observeLocation()
}
