package com.orbitalsonic.prayertimesample.presentation.prayer

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.orbitalsonic.prayertimesample.domain.model.LocationInfo
import com.orbitalsonic.prayertimesample.domain.model.NotificationSettings
import com.orbitalsonic.prayertimesample.domain.model.PrayerDayTimes
import com.orbitalsonic.prayertimesample.domain.model.PrayerName
import com.orbitalsonic.prayertimesample.domain.model.PrayerNotificationMode
import com.orbitalsonic.prayertimesample.domain.model.PrayerTimeModel
import com.orbitalsonic.prayertimesample.domain.repository.LocationRepository
import com.orbitalsonic.prayertimesample.domain.repository.NotificationSettingsRepository
import com.orbitalsonic.prayertimesample.domain.repository.PrayerTimeRepository
import com.orbitalsonic.prayertimesample.domain.usecase.CyclePrayerNotificationModeUseCase
import com.orbitalsonic.prayertimesample.domain.usecase.GetPrayerTimesUseCase
import com.orbitalsonic.prayertimesample.domain.usecase.ObserveLocationUseCase
import com.orbitalsonic.prayertimesample.domain.usecase.ObserveNotificationSettingsUseCase
import com.orbitalsonic.prayertimesample.domain.usecase.RefreshPrayerTimesUseCase
import com.orbitalsonic.prayertimesample.domain.usecase.SchedulePrayerAlarmsUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PrayerViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val prayerRepo = mockk<PrayerTimeRepository>()
    private val locationRepo = mockk<LocationRepository>()
    private val settingsRepo = mockk<NotificationSettingsRepository>()
    private val timesFlow = MutableStateFlow<PrayerDayTimes?>(null)
    private val locationFlow = MutableStateFlow<LocationInfo?>(null)
    private val settingsFlow = MutableStateFlow(NotificationSettings())

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { prayerRepo.observeTodayPrayerTimes() } returns timesFlow
        every { locationRepo.observeLocation() } returns locationFlow
        every { settingsRepo.observeSettings() } returns settingsFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun emitsSixPrayers_inOrder() = runTest {
        val prayers = PrayerName.ordered.mapIndexed { index, name ->
            PrayerTimeModel(name, "05:00 AM", index * 1000L, isNext = false)
        }
        timesFlow.value = PrayerDayTimes(System.currentTimeMillis(), prayers)
        locationFlow.value = LocationInfo(33.0, 73.0, "Islamabad")
        settingsFlow.value = NotificationSettings()

        val vm = PrayerViewModel(
            GetPrayerTimesUseCase(prayerRepo),
            RefreshPrayerTimesUseCase(
                prayerRepo,
                locationRepo,
                SchedulePrayerAlarmsUseCase(mockk(relaxed = true))
            ),
            ObserveLocationUseCase(locationRepo),
            ObserveNotificationSettingsUseCase(settingsRepo),
            CyclePrayerNotificationModeUseCase(settingsRepo, SchedulePrayerAlarmsUseCase(mockk(relaxed = true))),
            resolvingAddressLabel = "Resolving address…",
            locationUnavailableLabel = "Location unavailable"
        )
        advanceUntilIdle()
        assertEquals(6, vm.state.value.prayers.size)
        assertEquals("Dhuhr", vm.state.value.prayers[2].prayerName)
    }
}
