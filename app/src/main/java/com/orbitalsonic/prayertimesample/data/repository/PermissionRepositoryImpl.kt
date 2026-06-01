package com.orbitalsonic.prayertimesample.data.repository

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.orbitalsonic.prayertimesample.domain.model.PermissionStatus
import com.orbitalsonic.prayertimesample.domain.repository.PermissionRepository

class PermissionRepositoryImpl(
    private val context: Context,
    private val shouldShowRationale: (String) -> Boolean
) : PermissionRepository {

    override fun locationStatus(): PermissionStatus =
        permissionStatus(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

    override fun notificationStatus(): PermissionStatus {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return PermissionStatus.GRANTED
        }
        return permissionStatus(Manifest.permission.POST_NOTIFICATIONS)
    }

    override fun canScheduleExactAlarms(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        return alarmManager.canScheduleExactAlarms()
    }

    override fun isBatteryOptimizationExempt(): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    private fun permissionStatus(vararg permissions: String): PermissionStatus {
        val granted = permissions.any {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (granted) return PermissionStatus.GRANTED
        val permanentlyDenied = permissions.none { shouldShowRationale(it) }
        return if (permanentlyDenied) PermissionStatus.PERMANENTLY_DENIED else PermissionStatus.DENIED
    }
}
