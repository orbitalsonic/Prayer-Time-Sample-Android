package com.orbitalsonic.prayertimesample.presentation.prayer.viewmodel

import androidx.lifecycle.viewModelScope
import com.orbitalsonic.prayertimesample.domain.model.PrayerName
import com.orbitalsonic.prayertimesample.domain.usecase.CyclePrayerNotificationModeUseCase
import com.orbitalsonic.prayertimesample.domain.usecase.GetPrayerTimesUseCase
import com.orbitalsonic.prayertimesample.domain.usecase.ObserveLocationUseCase
import com.orbitalsonic.prayertimesample.domain.usecase.ObserveNotificationSettingsUseCase
import com.orbitalsonic.prayertimesample.domain.usecase.RefreshPrayerTimesUseCase
import com.orbitalsonic.prayertimesample.presentation.common.MviViewModel
import com.orbitalsonic.prayertimesample.presentation.prayer.contract.PrayerEffect
import com.orbitalsonic.prayertimesample.presentation.prayer.contract.PrayerIntent
import com.orbitalsonic.prayertimesample.presentation.prayer.contract.PrayerState
import com.orbitalsonic.prayertimesample.presentation.prayer.model.buildPrayerUiModels
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class PrayerViewModel(
    private val getPrayerTimesUseCase: GetPrayerTimesUseCase,
    private val refreshPrayerTimesUseCase: RefreshPrayerTimesUseCase,
    private val observeLocationUseCase: ObserveLocationUseCase,
    private val observeNotificationSettingsUseCase: ObserveNotificationSettingsUseCase,
    private val cyclePrayerNotificationModeUseCase: CyclePrayerNotificationModeUseCase,
    private val resolvingAddressLabel: String,
    private val locationUnavailableLabel: String
) : MviViewModel<PrayerIntent, PrayerState, PrayerEffect>(PrayerState()) {

    private var countdownJob: Job? = null
    private val dateFormat = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault())

    init {
        viewModelScope.launch {
            combine(
                getPrayerTimesUseCase(),
                observeLocationUseCase(),
                observeNotificationSettingsUseCase()
            ) { times, location, settings ->
                Triple(times, location, settings)
            }.collect { (times, location, settings) ->
                if (times == null) {
                    setState { copy(isLoading = true) }
                    return@collect
                }
                val modeMap = PrayerName.ordered.associateWith { settings.modeFor(it) }
                val uiPrayers = buildPrayerUiModels(times.prayers, modeMap)
                val next = times.prayers.firstOrNull { it.isNext }
                setState {
                    copy(
                        isLoading = false,
                        locationLabel = location?.displayAddress(
                            if (location.isValid) resolvingAddressLabel else locationUnavailableLabel
                        ) ?: locationUnavailableLabel,
                        dateLabel = dateFormat.format(Date(times.dateMillis)),
                        prayers = uiPrayers,
                        nextPrayerName = next?.name?.displayName.orEmpty(),
                        errorMessage = null
                    )
                }
                startCountdown(next?.timeMillis)
            }
        }
    }

    override fun onIntent(intent: PrayerIntent) {
        when (intent) {
            PrayerIntent.Load -> load()
            PrayerIntent.Refresh -> refresh()
            is PrayerIntent.CycleNotificationMode -> cycleMode(intent.prayer)
        }
    }

    private fun cycleMode(prayer: PrayerName) {
        viewModelScope.launch {
            cyclePrayerNotificationModeUseCase(prayer)
        }
    }

    private fun load() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            val result = refreshPrayerTimesUseCase(refreshLocation = true)
            if (result == null) {
                setState {
                    copy(
                        isLoading = false,
                        errorMessage = "Unable to calculate prayer times. Check location."
                    )
                }
                sendEffect(PrayerEffect.RequestLocationPermission)
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            refreshPrayerTimesUseCase(refreshLocation = true)
            setState { copy(isLoading = false) }
            sendEffect(PrayerEffect.ShowMessage("Prayer times refreshed"))
        }
    }

    private fun startCountdown(targetMillis: Long?) {
        countdownJob?.cancel()
        if (targetMillis == null || targetMillis <= 0L) return
        countdownJob = viewModelScope.launch {
            while (isActive) {
                val diff = targetMillis - System.currentTimeMillis()
                val text = if (diff <= 0) {
                    "00:00:00"
                } else {
                    formatCountdown(diff)
                }
                setState { copy(countdownText = text) }
                delay(1000)
            }
        }
    }

    private fun formatCountdown(millis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }
}