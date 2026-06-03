package com.orbitalsonic.prayertimesample.presentation.settings.permission

import com.orbitalsonic.prayertimesample.R
import com.orbitalsonic.prayertimesample.domain.model.PermissionStatus
import com.orbitalsonic.prayertimesample.domain.repository.PermissionRepository
import com.orbitalsonic.prayertimesample.presentation.settings.model.PermissionItem
import com.orbitalsonic.prayertimesample.presentation.settings.model.PermissionType

class PermissionStateChecker(
    private val permissionRepository: PermissionRepository
) {

    fun buildPermissionItems(): List<PermissionItem> {
        val locationStatus = permissionRepository.locationStatus()
        val notificationStatus = permissionRepository.notificationStatus()
        val exactAlarmGranted = permissionRepository.canScheduleExactAlarms()
        val batteryGranted = permissionRepository.isBatteryOptimizationExempt()

        return listOf(
            PermissionItem(
                type = PermissionType.LOCATION,
                titleRes = R.string.permission_location_title,
                subtitleRes = R.string.permission_location_subtitle,
                iconRes = R.drawable.ic_location_marker,
                isGranted = locationStatus == PermissionStatus.GRANTED,
                status = locationStatus
            ),
            PermissionItem(
                type = PermissionType.NOTIFICATION,
                titleRes = R.string.permission_notification_title,
                subtitleRes = R.string.permission_notification_subtitle,
                iconRes = R.drawable.ic_notification_prayer,
                isGranted = notificationStatus == PermissionStatus.GRANTED,
                status = notificationStatus
            ),
            PermissionItem(
                type = PermissionType.EXACT_ALARM,
                titleRes = R.string.permission_exact_alarm_title,
                subtitleRes = R.string.permission_exact_alarm_subtitle,
                iconRes = R.drawable.ic_notification_on,
                isGranted = exactAlarmGranted,
                status = if (exactAlarmGranted) PermissionStatus.GRANTED else PermissionStatus.DENIED
            ),
            PermissionItem(
                type = PermissionType.BATTERY_OPTIMIZATION,
                titleRes = R.string.permission_battery_title,
                subtitleRes = R.string.permission_battery_subtitle,
                iconRes = R.drawable.ic_settings,
                isGranted = batteryGranted,
                status = if (batteryGranted) PermissionStatus.GRANTED else PermissionStatus.DENIED
            )
        )
    }
}
