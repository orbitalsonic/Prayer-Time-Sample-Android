package com.orbitalsonic.prayertimesample.di

import android.app.Application
import androidx.fragment.app.FragmentActivity
import com.orbitalsonic.prayertimesample.data.alarm.AlarmSchedulerImpl
import com.orbitalsonic.prayertimesample.data.local.PrayerPreferencesDataStore
import com.orbitalsonic.prayertimesample.data.location.AddressResolver
import com.orbitalsonic.prayertimesample.data.location.FusedLocationDataSource
import com.orbitalsonic.prayertimesample.data.notification.AzanPlayerManager
import com.orbitalsonic.prayertimesample.data.prayer.SonicPrayerCalculator
import com.orbitalsonic.prayertimesample.data.repository.LocationRepositoryImpl
import com.orbitalsonic.prayertimesample.data.repository.NotificationSettingsRepositoryImpl
import com.orbitalsonic.prayertimesample.data.repository.PermissionRepositoryImpl
import com.orbitalsonic.prayertimesample.data.repository.PrayerTimeRepositoryImpl
import com.orbitalsonic.prayertimesample.domain.repository.AlarmScheduler
import com.orbitalsonic.prayertimesample.domain.repository.LocationRepository
import com.orbitalsonic.prayertimesample.domain.repository.NotificationSettingsRepository
import com.orbitalsonic.prayertimesample.domain.repository.PermissionRepository
import com.orbitalsonic.prayertimesample.domain.repository.PrayerTimeRepository
import com.orbitalsonic.prayertimesample.domain.usecase.CyclePrayerNotificationModeUseCase
import com.orbitalsonic.prayertimesample.domain.usecase.GetPrayerTimesUseCase
import com.orbitalsonic.prayertimesample.domain.usecase.ObserveLocationUseCase
import com.orbitalsonic.prayertimesample.domain.usecase.ObserveNotificationSettingsUseCase
import com.orbitalsonic.prayertimesample.domain.usecase.RefreshPrayerTimesUseCase
import com.orbitalsonic.prayertimesample.domain.usecase.SchedulePrayerAlarmsUseCase
import com.orbitalsonic.prayertimesample.R
import com.orbitalsonic.prayertimesample.presentation.location.LocationViewModel
import com.orbitalsonic.prayertimesample.presentation.prayer.viewmodel.PrayerViewModel
import com.orbitalsonic.prayertimesample.presentation.settings.viewmodel.SettingsViewModel

class AppContainer(private val application: Application) {

    val prayerPreferencesDataStore = PrayerPreferencesDataStore(application)
    private val calculator = SonicPrayerCalculator(prayerPreferencesDataStore)
    private val addressResolver = AddressResolver(application)
    private val fusedLocation = FusedLocationDataSource(application, addressResolver)

    val azanPlayerManager: AzanPlayerManager by lazy { AzanPlayerManager(application) }

    val locationRepository: LocationRepository by lazy {
        LocationRepositoryImpl(fusedLocation, prayerPreferencesDataStore, addressResolver)
    }

    val notificationSettingsRepository: NotificationSettingsRepository by lazy {
        NotificationSettingsRepositoryImpl(prayerPreferencesDataStore)
    }

    val prayerTimeRepository: PrayerTimeRepository by lazy {
        PrayerTimeRepositoryImpl(calculator, locationRepository, prayerPreferencesDataStore)
    }

    val alarmScheduler: AlarmScheduler by lazy {
        AlarmSchedulerImpl(
            application,
            prayerTimeRepository,
            locationRepository,
            notificationSettingsRepository
        )
    }

    fun permissionRepository(activity: FragmentActivity): PermissionRepository =
        PermissionRepositoryImpl(application) { permission ->
            activity.shouldShowRequestPermissionRationale(permission)
        }

    val schedulePrayerAlarmsUseCase by lazy { SchedulePrayerAlarmsUseCase(alarmScheduler) }

    val cyclePrayerNotificationModeUseCase by lazy {
        CyclePrayerNotificationModeUseCase(notificationSettingsRepository, schedulePrayerAlarmsUseCase)
    }

    val observeNotificationSettingsUseCase by lazy {
        ObserveNotificationSettingsUseCase(notificationSettingsRepository)
    }

    val refreshPrayerTimesUseCase by lazy {
        RefreshPrayerTimesUseCase(
            prayerTimeRepository,
            locationRepository,
            schedulePrayerAlarmsUseCase
        )
    }

    val getPrayerTimesUseCase by lazy { GetPrayerTimesUseCase(prayerTimeRepository) }
    val observeLocationUseCase by lazy { ObserveLocationUseCase(locationRepository) }

    fun prayerViewModel(): PrayerViewModel = PrayerViewModel(
        getPrayerTimesUseCase,
        refreshPrayerTimesUseCase,
        observeLocationUseCase,
        observeNotificationSettingsUseCase,
        cyclePrayerNotificationModeUseCase,
        application.getString(R.string.location_resolving_address),
        application.getString(R.string.location_unavailable)
    )

    fun settingsViewModel(activity: FragmentActivity): SettingsViewModel = SettingsViewModel(
        permissionRepository(activity)
    )

    fun locationViewModel(): LocationViewModel = LocationViewModel(
        observeLocationUseCase,
        locationRepository,
        refreshPrayerTimesUseCase
    )
}
