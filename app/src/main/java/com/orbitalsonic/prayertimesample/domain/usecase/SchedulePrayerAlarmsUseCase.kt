package com.orbitalsonic.prayertimesample.domain.usecase

import com.orbitalsonic.prayertimesample.domain.repository.AlarmScheduler

class SchedulePrayerAlarmsUseCase(
    private val alarmScheduler: AlarmScheduler
) {
    suspend operator fun invoke() {
        alarmScheduler.rescheduleAll()
    }
}
