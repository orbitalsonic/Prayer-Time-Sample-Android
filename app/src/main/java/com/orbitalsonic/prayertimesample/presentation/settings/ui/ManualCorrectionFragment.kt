package com.orbitalsonic.prayertimesample.presentation.settings.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.orbitalsonic.prayertimesample.PrayerTimeApp
import com.orbitalsonic.prayertimesample.R
import com.orbitalsonic.prayertimesample.databinding.FragmentPsManualCorrectionBinding
import kotlinx.coroutines.launch

class ManualCorrectionFragment : Fragment() {
    private var _binding: FragmentPsManualCorrectionBinding? = null
    private val binding get() = _binding!!
    private val dialogs = PrayerSettingDialogs()

    private val appContainer by lazy {
        (requireActivity().application as PrayerTimeApp).container
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPsManualCorrectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        bindClicks()
        refreshValues()
    }

    private fun bindClicks() {
        binding.btnFajrMinute.setOnClickListener {
            updateMinute(
                getString(R.string.prayer_fajr_text),
                { appContainer.prayerPreferencesDataStore.getPrayerManualCorrection().fajrMinute },
                { appContainer.prayerPreferencesDataStore.setFajrCorrectionMinutes(it) }
            )
        }
        binding.btnDhuhrMinute.setOnClickListener {
            updateMinute(
                getString(R.string.prayer_dhuhr_text),
                { appContainer.prayerPreferencesDataStore.getPrayerManualCorrection().zuhrMinute },
                { appContainer.prayerPreferencesDataStore.setDhuhrCorrectionMinutes(it) }
            )
        }
        binding.btnAsrMinute.setOnClickListener {
            updateMinute(
                getString(R.string.prayer_asr_text),
                { appContainer.prayerPreferencesDataStore.getPrayerManualCorrection().asrMinute },
                { appContainer.prayerPreferencesDataStore.setAsrCorrectionMinutes(it) }
            )
        }
        binding.btnMaghribMinute.setOnClickListener {
            updateMinute(
                getString(R.string.prayer_maghrib_text),
                { appContainer.prayerPreferencesDataStore.getPrayerManualCorrection().maghribMinute },
                { appContainer.prayerPreferencesDataStore.setMaghribCorrectionMinutes(it) }
            )
        }
        binding.btnIshaMinute.setOnClickListener {
            updateMinute(
                getString(R.string.prayer_isha_text),
                { appContainer.prayerPreferencesDataStore.getPrayerManualCorrection().ishaMinute },
                { appContainer.prayerPreferencesDataStore.setIshaCorrectionMinutes(it) }
            )
        }
    }

    private fun updateMinute(
        title: String,
        defaultProvider: suspend () -> Int,
        saver: suspend (Int) -> Unit
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            val defaultValue = defaultProvider()
            dialogs.showCorrectionMinutesDialog(this@ManualCorrectionFragment, title, defaultValue) { value ->
                viewLifecycleOwner.lifecycleScope.launch {
                    saver(value)
                    appContainer.refreshPrayerTimesUseCase()
                    refreshValues()
                }
            }
        }
    }

    private fun refreshValues() {
        viewLifecycleOwner.lifecycleScope.launch {
            val correction = appContainer.prayerPreferencesDataStore.getPrayerManualCorrection()
            binding.mtvFajrMinute.text = getString(R.string.minutes_format, correction.fajrMinute)
            binding.mtvDhuhrMinute.text = getString(R.string.minutes_format, correction.zuhrMinute)
            binding.mtvAsrMinute.text = getString(R.string.minutes_format, correction.asrMinute)
            binding.mtvMaghribMinute.text = getString(R.string.minutes_format, correction.maghribMinute)
            binding.mtvIshaMinute.text = getString(R.string.minutes_format, correction.ishaMinute)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}