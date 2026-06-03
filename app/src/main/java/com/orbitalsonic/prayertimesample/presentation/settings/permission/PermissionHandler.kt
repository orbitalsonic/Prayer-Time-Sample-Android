package com.orbitalsonic.prayertimesample.presentation.settings.permission

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import com.orbitalsonic.prayertimesample.R
import com.orbitalsonic.prayertimesample.domain.repository.PermissionRepository
import com.orbitalsonic.prayertimesample.presentation.settings.model.PermissionItem
import com.orbitalsonic.prayertimesample.presentation.settings.model.PermissionType

class PermissionHandler(
    private val fragment: Fragment,
    private val permissionRepository: PermissionRepository,
    private val locationPermissionLauncher: ActivityResultLauncher<Array<String>>,
    private val notificationPermissionLauncher: ActivityResultLauncher<String>,
    private val onPermissionsChanged: () -> Unit
) {

    var pendingExactAlarmExplanation: Boolean = false
        private set
    var pendingBatteryExplanation: Boolean = false
        private set

    fun handlePermissionTap(item: PermissionItem) {
        if (item.isGranted) {
            showManagePermissionDialog(item)
            return
        }
        when (item.type) {
            PermissionType.LOCATION -> handleLocation(item)
            PermissionType.NOTIFICATION -> handleNotification(item)
            PermissionType.EXACT_ALARM -> handleExactAlarm()
            PermissionType.BATTERY_OPTIMIZATION -> handleBatteryOptimization()
        }
    }

    fun onResume() {
        if (pendingExactAlarmExplanation) {
            pendingExactAlarmExplanation = false
            if (!permissionRepository.canScheduleExactAlarms()) {
                showExactAlarmRequiredDialog()
            }
        }
        if (pendingBatteryExplanation) {
            pendingBatteryExplanation = false
            if (!permissionRepository.isBatteryOptimizationExempt()) {
                showBatteryRequiredDialog()
            }
        }
        onPermissionsChanged()
    }

    private fun handleLocation(@Suppress("UNUSED_PARAMETER") item: PermissionItem) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun handleNotification(item: PermissionItem) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            onPermissionsChanged()
            return
        }
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    fun onLocationPermissionResult(granted: Boolean) {
        onPermissionsChanged()
        if (!granted && permissionRepository.isLocationPermanentlyDenied()) {
            showLocationRequiredDialog()
        }
    }

    fun onNotificationPermissionResult(granted: Boolean) {
        onPermissionsChanged()
        if (!granted && permissionRepository.isNotificationPermanentlyDenied()) {
            showNotificationRequiredDialog()
        }
    }

    private fun handleExactAlarm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingExactAlarmExplanation = true
            fragment.startActivity(
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${fragment.requireContext().packageName}")
                }
            )
        } else {
            onPermissionsChanged()
        }
    }

    private fun handleBatteryOptimization() {
        pendingBatteryExplanation = true
        val packageName = fragment.requireContext().packageName
        fragment.startActivity(
            Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:$packageName")
            }
        )
    }

    private fun showManagePermissionDialog(item: PermissionItem) {
        PermissionDialog.show(
            fragment = fragment,
            config = PermissionDialogConfig(
                titleRes = R.string.permission_manage_title,
                messageRes = R.string.permission_manage_message,
                iconRes = item.iconRes
            ),
            onOpenSettings = { openAppSettings() }
        )
    }

    private fun showLocationRequiredDialog() {
        PermissionDialog.show(
            fragment = fragment,
            config = PermissionDialogConfig(
                titleRes = R.string.permission_location_required_title,
                messageRes = R.string.permission_location_required_message,
                iconRes = R.drawable.ic_location_marker
            ),
            onOpenSettings = { openAppSettings() }
        )
    }

    private fun showNotificationRequiredDialog() {
        PermissionDialog.show(
            fragment = fragment,
            config = PermissionDialogConfig(
                titleRes = R.string.permission_notification_required_title,
                messageRes = R.string.permission_notification_required_message,
                iconRes = R.drawable.ic_notification_prayer
            ),
            onOpenSettings = { openAppSettings() }
        )
    }

    private fun showExactAlarmRequiredDialog() {
        PermissionDialog.show(
            fragment = fragment,
            config = PermissionDialogConfig(
                titleRes = R.string.permission_exact_alarm_required_title,
                messageRes = R.string.permission_exact_alarm_required_message,
                iconRes = R.drawable.ic_notification_on
            ),
            onOpenSettings = { openExactAlarmSettings() }
        )
    }

    private fun showBatteryRequiredDialog() {
        PermissionDialog.show(
            fragment = fragment,
            config = PermissionDialogConfig(
                titleRes = R.string.permission_battery_required_title,
                messageRes = R.string.permission_battery_required_message,
                iconRes = R.drawable.ic_settings
            ),
            onOpenSettings = { openBatteryOptimizationSettings() }
        )
    }

    private fun openAppSettings() {
        val packageName = fragment.requireContext().packageName
        fragment.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
        )
    }

    private fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            fragment.startActivity(
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${fragment.requireContext().packageName}")
                }
            )
        }
    }

    private fun openBatteryOptimizationSettings() {
        val packageName = fragment.requireContext().packageName
        fragment.startActivity(
            Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:$packageName")
            }
        )
    }
}
