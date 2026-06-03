package com.orbitalsonic.prayertimesample.presentation.settings.ui

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.orbitalsonic.prayertimesample.PrayerTimeApp
import com.orbitalsonic.prayertimesample.R
import com.orbitalsonic.prayertimesample.databinding.FragmentSettingsBinding
import com.orbitalsonic.prayertimesample.presentation.settings.adapters.PermissionListAdapter
import com.orbitalsonic.prayertimesample.presentation.settings.contract.SettingsIntent
import com.orbitalsonic.prayertimesample.presentation.settings.contract.SettingsState
import com.orbitalsonic.prayertimesample.presentation.settings.permission.PermissionHandler
import com.orbitalsonic.prayertimesample.presentation.settings.viewmodel.SettingsViewModel
import com.orbitalsonic.sonicopt.enums.AsrJuristicMethod
import com.orbitalsonic.sonicopt.enums.HighLatitudeAdjustment
import com.orbitalsonic.sonicopt.enums.PrayerTimeConvention
import com.orbitalsonic.sonicopt.enums.TimeFormat
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val prayerSettingDialogs = PrayerSettingDialogs()

    private val appContainer by lazy {
        (requireActivity().application as PrayerTimeApp).container
    }

    private val viewModel: SettingsViewModel by lazy {
        appContainer.settingsViewModel(requireActivity())
    }

    private lateinit var permissionHandler: PermissionHandler

    private val permissionAdapter = PermissionListAdapter { item ->
        permissionHandler.handlePermissionTap(item)
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionHandler.onNotificationPermissionResult(granted)
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        permissionHandler.onLocationPermissionResult(granted)
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

        permissionHandler = PermissionHandler(
            fragment = this,
            permissionRepository = appContainer.permissionRepository(requireActivity()),
            locationPermissionLauncher = locationPermissionLauncher,
            notificationPermissionLauncher = notificationPermissionLauncher,
            onPermissionsChanged = { viewModel.onIntent(SettingsIntent.RefreshPermissions) }
        )

        binding.permissionsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.permissionsRecycler.adapter = permissionAdapter
        binding.permissionsRecycler.itemAnimator = null

        binding.btnBack.setOnClickListener {
            if (isAdded) {
                findNavController().popBackStack()
            }
        }
        setupPrayerSettingClicks()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.state.collect { render(it) } }
                launch { refreshPrayerSettingSummary() }
            }
        }

        viewModel.onIntent(SettingsIntent.RefreshPermissions)
    }

    private fun render(state: SettingsState) {
        permissionAdapter.submitList(state.permissionItems)
    }

    private fun setupPrayerSettingClicks() {
        binding.btnPrayerTimeConvention.setOnClickListener {
            findNavController().navigate(R.id.action_settings_to_prayerConventionFragment)
        }
        binding.btnManualCorrection.setOnClickListener {
            findNavController().navigate(R.id.action_settings_to_manualCorrectionFragment)
        }
        binding.btnAsrTimeCalculation.setOnClickListener {
            prayerSettingDialogs.showAsrCalculationDialog(
                fragment = this,
                dataStore = appContainer.prayerPreferencesDataStore
            ) {
                viewLifecycleOwner.lifecycleScope.launch {
                    appContainer.refreshPrayerTimesUseCase()
                    refreshPrayerSettingSummary()
                }
            }
        }
        binding.btnHighLatitudeAdjustment.setOnClickListener {
            prayerSettingDialogs.showHighLatDialog(
                fragment = this,
                dataStore = appContainer.prayerPreferencesDataStore
            ) {
                viewLifecycleOwner.lifecycleScope.launch {
                    appContainer.refreshPrayerTimesUseCase()
                    refreshPrayerSettingSummary()
                }
            }
        }
        binding.btnTimeFormat.setOnClickListener {
            prayerSettingDialogs.showTimeFormatDialog(
                fragment = this,
                dataStore = appContainer.prayerPreferencesDataStore
            ) {
                viewLifecycleOwner.lifecycleScope.launch {
                    appContainer.refreshPrayerTimesUseCase()
                    refreshPrayerSettingSummary()
                }
            }
        }
    }

    private suspend fun refreshPrayerSettingSummary() {
        val store = appContainer.prayerPreferencesDataStore
        val convention = store.getPrayerTimeConvention()
        val asr = store.getAsrJuristicMethod()
        val corrections = store.getPrayerManualCorrection()
        val highLat = store.getHighLatitudeAdjustment()
        val format = store.getTimeFormat()

        binding.mtvPtcOrganization.text = conventionName(convention)
        binding.mtvAtcJuristicMethod.text = if (asr == AsrJuristicMethod.HANAFI) {
            getString(R.string.atc_hanafi)
        } else {
            getString(R.string.atc_shafi)
        }
        binding.mtvManualCorrection.text = listOf(
            corrections.fajrMinute,
            corrections.zuhrMinute,
            corrections.asrMinute,
            corrections.maghribMinute,
            corrections.ishaMinute
        ).joinToString(",")
        binding.mtvHlaAdjustments.text = when (highLat) {
            HighLatitudeAdjustment.NO_ADJUSTMENT -> getString(R.string.hla_no_adjustment)
            HighLatitudeAdjustment.MID_NIGHT -> getString(R.string.hla_mid_night)
            HighLatitudeAdjustment.ONE_SEVENTH -> getString(R.string.hla_one_seventh)
            HighLatitudeAdjustment.TWILIGHT_ANGLE -> getString(R.string.hla_twilight_angle)
        }
        binding.mtvTfTimeFormat.text = when (format) {
            TimeFormat.HOUR_12 -> getString(R.string.tf_12_hour)
            TimeFormat.HOUR_12_NS -> getString(R.string.tf_12_hour_ns)
            TimeFormat.HOUR_24 -> getString(R.string.tf_24_hour)
            TimeFormat.FLOATING -> getString(R.string.tf_floating)
        }
    }

    private fun conventionName(convention: PrayerTimeConvention): String = when (convention) {
        PrayerTimeConvention.MWL -> getString(R.string.ptc_mwl)
        PrayerTimeConvention.EGYPT -> getString(R.string.ptc_egypt)
        PrayerTimeConvention.KARACHI -> getString(R.string.ptc_karachi)
        PrayerTimeConvention.MAKKAH -> getString(R.string.ptc_makkah)
        PrayerTimeConvention.DUBAI -> getString(R.string.ptc_dubai)
        PrayerTimeConvention.MOONSIGHTING_COMMITTEE -> getString(R.string.ptc_moonsighting_committee)
        PrayerTimeConvention.ISNA -> getString(R.string.ptc_isna)
        PrayerTimeConvention.KUWAIT -> getString(R.string.ptc_kuwait)
        PrayerTimeConvention.QATAR -> getString(R.string.ptc_qatar)
        PrayerTimeConvention.SINGAPORE -> getString(R.string.ptc_singapore)
        PrayerTimeConvention.TEHRAN -> getString(R.string.ptc_tehran)
        PrayerTimeConvention.JAFFARI -> getString(R.string.ptc_jafari)
        PrayerTimeConvention.GULF_REGION -> getString(R.string.ptc_gulf_region)
        PrayerTimeConvention.FRANCE -> getString(R.string.ptc_france)
        PrayerTimeConvention.TURKEY -> getString(R.string.ptc_turkey)
        PrayerTimeConvention.RUSSIA -> getString(R.string.ptc_russia)
        PrayerTimeConvention.CUSTOM -> getString(R.string.ptc_custom)
    }

    override fun onResume() {
        super.onResume()
        permissionHandler.onResume()
        viewModel.onIntent(SettingsIntent.RefreshPermissions)
        viewLifecycleOwner.lifecycleScope.launch {
            refreshPrayerSettingSummary()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
