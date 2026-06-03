package com.orbitalsonic.prayertimesample.presentation.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.orbitalsonic.prayertimesample.PrayerTimeApp
import com.orbitalsonic.prayertimesample.R
import com.orbitalsonic.prayertimesample.databinding.FragmentPsCustomAngleBinding
import com.orbitalsonic.sonicopt.enums.PrayerTimeConvention
import kotlinx.coroutines.launch

class CustomAngleFragment : Fragment() {

    private var _binding: FragmentPsCustomAngleBinding? = null
    private val binding get() = _binding!!

    private val appContainer by lazy {
        (requireActivity().application as PrayerTimeApp).container
    }

    private val dialogs = PrayerSettingDialogs()

    private val customAngleList = listOf(
        9.0, 9.5, 10.0, 10.5, 11.0, 11.5, 12.0, 12.5, 13.0, 13.5, 14.0, 14.5, 15.0, 15.5,
        16.0, 16.5, 17.0, 17.5, 18.0, 18.5, 19.0, 19.5, 20.0, 20.5, 21.0, 21.5, 22.0, 22.5,
        23.0, 23.5
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPsCustomAngleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        binding.btnFajrAngle.setOnClickListener { showFajrAngleDialog() }
        binding.btnIshaAngle.setOnClickListener { showIshaAngleDialog() }
        refreshValues()
    }

    private fun refreshValues() {
        viewLifecycleOwner.lifecycleScope.launch {
            val angles = appContainer.prayerPreferencesDataStore.getPrayerCustomAngle()
            binding.mtvFajrAngle.text = getString(R.string.angle_format, angles.fajrAngle)
            binding.mtvIshaAngle.text = getString(R.string.angle_format, angles.ishaAngle)
        }
    }

    private fun showFajrAngleDialog() {
        viewLifecycleOwner.lifecycleScope.launch {
            val current = appContainer.prayerPreferencesDataStore.getPrayerCustomAngle().fajrAngle
            dialogs.showPrayerAngleDialog(
                fragment = this@CustomAngleFragment,
                title = getString(R.string.prayer_fajr_text),
                items = customAngleList,
                defaultSelection = current
            ) { selected ->
                viewLifecycleOwner.lifecycleScope.launch {
                    val isha = appContainer.prayerPreferencesDataStore.getPrayerCustomAngle().ishaAngle
                    appContainer.prayerPreferencesDataStore.setPrayerCustomAngle(selected, isha)
                    appContainer.prayerPreferencesDataStore.setPrayerTimeConvention(PrayerTimeConvention.CUSTOM)
                    appContainer.refreshPrayerTimesUseCase()
                    refreshValues()
                }
            }
        }
    }

    private fun showIshaAngleDialog() {
        viewLifecycleOwner.lifecycleScope.launch {
            val current = appContainer.prayerPreferencesDataStore.getPrayerCustomAngle().ishaAngle
            dialogs.showPrayerAngleDialog(
                fragment = this@CustomAngleFragment,
                title = getString(R.string.prayer_isha_text),
                items = customAngleList,
                defaultSelection = current
            ) { selected ->
                viewLifecycleOwner.lifecycleScope.launch {
                    val fajr = appContainer.prayerPreferencesDataStore.getPrayerCustomAngle().fajrAngle
                    appContainer.prayerPreferencesDataStore.setPrayerCustomAngle(fajr, selected)
                    appContainer.prayerPreferencesDataStore.setPrayerTimeConvention(PrayerTimeConvention.CUSTOM)
                    appContainer.refreshPrayerTimesUseCase()
                    refreshValues()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
