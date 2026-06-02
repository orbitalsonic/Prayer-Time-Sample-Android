package com.orbitalsonic.prayertimesample.presentation.prayer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.orbitalsonic.prayertimesample.PrayerTimeApp
import com.orbitalsonic.prayertimesample.R
import com.orbitalsonic.prayertimesample.databinding.FragmentPrayerBinding
import com.orbitalsonic.prayertimesample.lifecycle.TimeChangeLifecycleObserver
import kotlinx.coroutines.launch

class PrayerFragment : Fragment() {

    private var _binding: FragmentPrayerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PrayerViewModel by lazy {
        (requireActivity().application as PrayerTimeApp).container.prayerViewModel()
    }

    private val adapter by lazy {
        PrayerListAdapter { prayer ->
            viewModel.onIntent(PrayerIntent.CycleNotificationMode(prayer))
        }
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) viewModel.onIntent(PrayerIntent.Refresh)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val app = requireActivity().application as PrayerTimeApp
        lifecycle.addObserver(
            TimeChangeLifecycleObserver(app.container, viewLifecycleOwner.lifecycleScope)
        )

        binding.prayerList.adapter = adapter
        binding.btnRefresh.setOnClickListener { viewModel.onIntent(PrayerIntent.Refresh) }
        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_prayer_to_settings)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collect { render(it) }
                }
                launch {
                    viewModel.effects.collect { effect ->
                        when (effect) {
                            is PrayerEffect.ShowMessage ->
                                Toast.makeText(requireContext(), effect.message, Toast.LENGTH_SHORT).show()
                            PrayerEffect.RequestLocationPermission -> requestLocationIfNeeded()
                        }
                    }
                }
            }
        }

        if (hasLocationPermission()) {
            viewModel.onIntent(PrayerIntent.Load)
        } else {
            requestLocationIfNeeded()
        }

    }

    private fun render(state: PrayerState) {
        binding.progress.isVisible = state.isLoading
        binding.locationText.text = state.locationLabel
        binding.dateText.text = state.dateLabel
        binding.nextPrayerLabel.text = getString(
            R.string.next_prayer_label,
            state.nextPrayerName.ifEmpty { "-" }
        )
        binding.countdownText.text = state.countdownText
        adapter.submitList(state.prayers)
        state.errorMessage?.let {
            binding.errorText.isVisible = true
            binding.errorText.text = it
        } ?: run { binding.errorText.isVisible = false }
    }

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    private fun requestLocationIfNeeded() {
        if (!hasLocationPermission()) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
