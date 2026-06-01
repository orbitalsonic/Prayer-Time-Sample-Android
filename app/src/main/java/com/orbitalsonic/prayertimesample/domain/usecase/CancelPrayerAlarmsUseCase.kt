package com.orbitalsonic.prayertimesample.domain.usecase

import com.orbitalsonic.prayertimesample.domain.repository.AlarmScheduler

class CancelPrayerAlarmsUseCase(
    private val alarmScheduler: AlarmScheduler
) {
    operator fun invoke() {
        alarmScheduler.cancelAll()
    }
}
