package com.orbitalsonic.prayertimesample.presentation.settings

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.orbitalsonic.prayertimesample.PrayerTimeApp
import com.orbitalsonic.prayertimesample.R
import com.orbitalsonic.prayertimesample.databinding.FragmentSettingsBinding
import com.orbitalsonic.prayertimesample.domain.model.PermissionStatus
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by lazy {
        (requireActivity().application as PrayerTimeApp)
            .container
            .settingsViewModel(requireActivity())
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        viewModel.onIntent(SettingsIntent.RefreshPermissions)
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.onIntent(SettingsIntent.RefreshPermissions)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBatterySettings.setOnClickListener { openBatterySettings() }
        binding.btnExactAlarm.setOnClickListener { openExactAlarmSettings() }
        binding.btnLocationPermission.setOnClickListener { requestLocation() }
        binding.btnNotificationPermission.setOnClickListener { requestNotification() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { render(it) }
            }
        }

        viewModel.onIntent(SettingsIntent.RefreshPermissions)
    }

    private fun render(state: SettingsState) {
        binding.batteryStatus.text = if (state.batteryExempt) {
            getString(R.string.status_granted)
        } else {
            getString(R.string.status_denied)
        }
        renderPermissionStatus(binding.locationStatus, state.permissionRows.getOrNull(0)?.status)
        renderPermissionStatus(binding.notificationStatus, state.permissionRows.getOrNull(1)?.status)
        renderPermissionStatus(binding.exactAlarmStatus, state.permissionRows.getOrNull(2)?.status)
    }

    private fun renderPermissionStatus(
        view: android.widget.TextView,
        status: PermissionStatus?
    ) {
        view.text = when (status) {
            PermissionStatus.GRANTED -> getString(R.string.status_granted)
            PermissionStatus.PERMANENTLY_DENIED -> getString(R.string.status_permanently_denied)
            PermissionStatus.DENIED, null -> getString(R.string.status_denied)
        }
    }

    private fun requestLocation() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun requestNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            Toast.makeText(requireContext(), R.string.status_granted, Toast.LENGTH_SHORT).show()
        }
    }

    private fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${requireContext().packageName}")
            })
        }
    }

    private fun openBatterySettings() {
        startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
    }

    override fun onResume() {
        super.onResume()
        viewModel.onIntent(SettingsIntent.RefreshPermissions)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
