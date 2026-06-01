package com.orbitalsonic.prayertimesample.lifecycle

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.orbitalsonic.prayertimesample.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class TimeChangeLifecycleObserver(
    private val container: AppContainer,
    private val scope: CoroutineScope
) : DefaultLifecycleObserver {

    private var lastDayOfYear: Int = -1
    private var lastTimezone: String = ""

    override fun onResume(owner: LifecycleOwner) {
        val calendar = java.util.Calendar.getInstance()
        val day = calendar.get(java.util.Calendar.DAY_OF_YEAR)
        val tz = calendar.timeZone.id
        if (lastDayOfYear != -1 && (day != lastDayOfYear || tz != lastTimezone)) {
            scope.launch {
                container.refreshPrayerTimesUseCase()
            }
        }
        lastDayOfYear = day
        lastTimezone = tz
    }
}
