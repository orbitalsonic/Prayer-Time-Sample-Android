package com.orbitalsonic.prayertimesample.presentation.location

import androidx.lifecycle.viewModelScope
import com.orbitalsonic.prayertimesample.domain.repository.LocationRepository
import com.orbitalsonic.prayertimesample.domain.usecase.ObserveLocationUseCase
import com.orbitalsonic.prayertimesample.domain.usecase.RefreshPrayerTimesUseCase
import com.orbitalsonic.prayertimesample.presentation.common.MviViewModel
import kotlinx.coroutines.launch

class LocationViewModel(
    private val observeLocationUseCase: ObserveLocationUseCase,
    private val locationRepository: LocationRepository,
    private val refreshPrayerTimesUseCase: RefreshPrayerTimesUseCase
) : MviViewModel<LocationIntent, LocationState, LocationEffect>(LocationState()) {

    init {
        viewModelScope.launch {
            observeLocationUseCase().collect { location ->
                if (location != null) {
                    setState {
                        copy(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            address = location.address,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    override fun onIntent(intent: LocationIntent) {
        when (intent) {
            LocationIntent.Refresh -> refresh()
            LocationIntent.LoadCached -> loadCached()
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            val location = locationRepository.refreshLocation()
            if (location == null) {
                setState { copy(isLoading = false) }
                sendEffect(LocationEffect.ShowError("Unable to get location"))
                sendEffect(LocationEffect.RequestPermission)
            } else {
                refreshPrayerTimesUseCase(refreshLocation = false)
                setState { copy(isLoading = false) }
            }
        }
    }

    private fun loadCached() {
        viewModelScope.launch {
            locationRepository.getCachedLocation()
        }
    }
}
