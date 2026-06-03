package com.orbitalsonic.prayertimesample.presentation.settings.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.orbitalsonic.prayertimesample.PrayerTimeApp
import com.orbitalsonic.prayertimesample.R
import com.orbitalsonic.prayertimesample.databinding.FragmentPrayerConventionBinding
import com.orbitalsonic.prayertimesample.presentation.settings.adapters.ConventionAdapter
import com.orbitalsonic.prayertimesample.presentation.settings.model.ConventionItem
import com.orbitalsonic.sonicopt.enums.PrayerTimeConvention
import kotlinx.coroutines.launch

class PrayerConventionFragment : Fragment() {
    private var _binding: FragmentPrayerConventionBinding? = null
    private val binding get() = _binding!!

    private val dataStore by lazy {
        (requireActivity().application as PrayerTimeApp).container
    }

    private val adapter by lazy {
        ConventionAdapter { onConventionClicked(it) }
    }

    private val ptcOrganizationNames by lazy {
        mapOf(
            PrayerTimeConvention.MWL to getString(R.string.ptc_mwl),
            PrayerTimeConvention.EGYPT to getString(R.string.ptc_egypt),
            PrayerTimeConvention.KARACHI to getString(R.string.ptc_karachi),
            PrayerTimeConvention.MAKKAH to getString(R.string.ptc_makkah),
            PrayerTimeConvention.DUBAI to getString(R.string.ptc_dubai),
            PrayerTimeConvention.MOONSIGHTING_COMMITTEE to getString(R.string.ptc_moonsighting_committee),
            PrayerTimeConvention.ISNA to getString(R.string.ptc_isna),
            PrayerTimeConvention.KUWAIT to getString(R.string.ptc_kuwait),
            PrayerTimeConvention.QATAR to getString(R.string.ptc_qatar),
            PrayerTimeConvention.SINGAPORE to getString(R.string.ptc_singapore),
            PrayerTimeConvention.TEHRAN to getString(R.string.ptc_tehran),
            PrayerTimeConvention.JAFFARI to getString(R.string.ptc_jafari),
            PrayerTimeConvention.GULF_REGION to getString(R.string.ptc_gulf_region),
            PrayerTimeConvention.FRANCE to getString(R.string.ptc_france),
            PrayerTimeConvention.TURKEY to getString(R.string.ptc_turkey),
            PrayerTimeConvention.RUSSIA to getString(R.string.ptc_russia),
            PrayerTimeConvention.CUSTOM to getString(R.string.ptc_custom)
        )
    }

    private val ptcOrganizationAngles by lazy {
        mapOf(
            PrayerTimeConvention.MWL to getString(R.string.ptc_mwl_angle),
            PrayerTimeConvention.EGYPT to getString(R.string.ptc_egypt_angle),
            PrayerTimeConvention.KARACHI to getString(R.string.ptc_karachi_angle),
            PrayerTimeConvention.MAKKAH to getString(R.string.ptc_makkah_angle),
            PrayerTimeConvention.DUBAI to getString(R.string.ptc_dubai_angle),
            PrayerTimeConvention.MOONSIGHTING_COMMITTEE to getString(R.string.ptc_moonsighting_committee_angle),
            PrayerTimeConvention.ISNA to getString(R.string.ptc_isna_angle),
            PrayerTimeConvention.KUWAIT to getString(R.string.ptc_kuwait_angle),
            PrayerTimeConvention.QATAR to getString(R.string.ptc_qatar_angle),
            PrayerTimeConvention.SINGAPORE to getString(R.string.ptc_singapore_angle),
            PrayerTimeConvention.TEHRAN to getString(R.string.ptc_tehran_angle),
            PrayerTimeConvention.JAFFARI to getString(R.string.ptc_jafari_angle),
            PrayerTimeConvention.GULF_REGION to getString(R.string.ptc_gulf_region_angle),
            PrayerTimeConvention.FRANCE to getString(R.string.ptc_france_angle),
            PrayerTimeConvention.TURKEY to getString(R.string.ptc_turkey_angle),
            PrayerTimeConvention.RUSSIA to getString(R.string.ptc_russia_angle)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrayerConventionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.conventionRecyclerview.adapter = adapter
        binding.btnBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        refreshList()
    }

    private fun refreshList() {
        viewLifecycleOwner.lifecycleScope.launch {
            val store = dataStore.prayerPreferencesDataStore
            val selected = store.getPrayerTimeConvention()
            val customAngle = store.getPrayerCustomAngle()
            val items = ptcOrganizationNames.map { (convention, title) ->
                val subtitle = if (convention == PrayerTimeConvention.CUSTOM) {
                    "${getString(R.string.prayer_fajr_text)}: ${customAngle.fajrAngle}°, ${getString(
                        R.string.prayer_isha_text)}: ${customAngle.ishaAngle}°"
                } else {
                    ptcOrganizationAngles[convention].orEmpty()
                }
                ConventionItem(
                    convention = convention,
                    title = title,
                    subtitle = subtitle,
                    isSelected = convention == selected
                )
            }
            adapter.submitList(items)
        }
    }

    private fun onConventionClicked(item: ConventionItem) {
        viewLifecycleOwner.lifecycleScope.launch {
            dataStore.prayerPreferencesDataStore.setPrayerTimeConvention(item.convention)
            dataStore.refreshPrayerTimesUseCase()
            refreshList()
            if (item.convention == PrayerTimeConvention.CUSTOM) {
                findNavController().navigate(R.id.action_prayerConventionFragment_to_customAngleFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.conventionRecyclerview.adapter = null
        _binding = null
    }
}