package com.orbitalsonic.prayertimesample.domain.repository

import com.orbitalsonic.prayertimesample.domain.model.PermissionStatus

interface PermissionRepository {
    fun locationStatus(): PermissionStatus
    fun notificationStatus(): PermissionStatus
    fun canScheduleExactAlarms(): Boolean
    fun isBatteryOptimizationExempt(): Boolean
    fun isLocationPermanentlyDenied(): Boolean
    fun isNotificationPermanentlyDenied(): Boolean
}
