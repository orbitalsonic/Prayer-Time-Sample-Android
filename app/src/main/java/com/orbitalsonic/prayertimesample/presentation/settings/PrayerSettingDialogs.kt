package com.orbitalsonic.prayertimesample.presentation.settings

import android.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.orbitalsonic.prayertimesample.R
import com.orbitalsonic.prayertimesample.data.local.PrayerPreferencesDataStore
import com.orbitalsonic.prayertimesample.databinding.DialogAsrSettingsBinding
import com.orbitalsonic.prayertimesample.databinding.DialogPrayerHighLatBinding
import com.orbitalsonic.prayertimesample.databinding.DialogPrayerTimeFormatBinding
import com.orbitalsonic.sonicopt.enums.AsrJuristicMethod
import com.orbitalsonic.sonicopt.enums.HighLatitudeAdjustment
import com.orbitalsonic.sonicopt.enums.TimeFormat
import kotlinx.coroutines.launch

class PrayerSettingDialogs {
    fun showAsrCalculationDialog(
        fragment: Fragment,
        dataStore: PrayerPreferencesDataStore,
        onSaved: (AsrJuristicMethod) -> Unit
    ) {
        val activity = fragment.activity ?: return
        val binding = DialogAsrSettingsBinding.inflate(activity.layoutInflater)
        val dialog = AlertDialog.Builder(activity).setView(binding.root).setCancelable(false).create()
        var selected = AsrJuristicMethod.HANAFI

        fragment.viewLifecycleOwner.lifecycleScope.launch {
            selected = dataStore.getAsrJuristicMethod()
            renderAsrSelection(binding, selected)
            binding.btnHanafi.setOnClickListener {
                selected = AsrJuristicMethod.HANAFI
                renderAsrSelection(binding, selected)
            }
            binding.btnShafi.setOnClickListener {
                selected = AsrJuristicMethod.SHAFI
                renderAsrSelection(binding, selected)
            }
            binding.buttonDone.setOnClickListener {
                fragment.viewLifecycleOwner.lifecycleScope.launch {
                    dataStore.setAsrJuristicMethod(selected)
                    onSaved(selected)
                }
                dialog.dismiss()
            }
            binding.buttonCancel.setOnClickListener { dialog.dismiss() }
            dialog.show()
        }
    }

    fun showHighLatDialog(
        fragment: Fragment,
        dataStore: PrayerPreferencesDataStore,
        onSaved: (HighLatitudeAdjustment) -> Unit
    ) {
        val activity = fragment.activity ?: return
        val binding = DialogPrayerHighLatBinding.inflate(activity.layoutInflater)
        val dialog = AlertDialog.Builder(activity).setView(binding.root).setCancelable(false).create()
        var selected = HighLatitudeAdjustment.NO_ADJUSTMENT

        fragment.viewLifecycleOwner.lifecycleScope.launch {
            selected = dataStore.getHighLatitudeAdjustment()
            renderHighLatSelection(binding, selected)
            binding.btnNoAdjustment.setOnClickListener {
                selected = HighLatitudeAdjustment.NO_ADJUSTMENT
                renderHighLatSelection(binding, selected)
            }
            binding.btnMiddleOfNight.setOnClickListener {
                selected = HighLatitudeAdjustment.MID_NIGHT
                renderHighLatSelection(binding, selected)
            }
            binding.btnSeventhOfNight.setOnClickListener {
                selected = HighLatitudeAdjustment.ONE_SEVENTH
                renderHighLatSelection(binding, selected)
            }
            binding.btnTwilightAngle.setOnClickListener {
                selected = HighLatitudeAdjustment.TWILIGHT_ANGLE
                renderHighLatSelection(binding, selected)
            }
            binding.buttonDone.setOnClickListener {
                fragment.viewLifecycleOwner.lifecycleScope.launch {
                    dataStore.setHighLatitudeAdjustment(selected)
                    onSaved(selected)
                }
                dialog.dismiss()
            }
            binding.buttonCancel.setOnClickListener { dialog.dismiss() }
            dialog.show()
        }
    }

    fun showTimeFormatDialog(
        fragment: Fragment,
        dataStore: PrayerPreferencesDataStore,
        onSaved: (TimeFormat) -> Unit
    ) {
        val activity = fragment.activity ?: return
        val binding = DialogPrayerTimeFormatBinding.inflate(activity.layoutInflater)
        val dialog = AlertDialog.Builder(activity).setView(binding.root).setCancelable(false).create()
        var selected = TimeFormat.HOUR_12

        fragment.viewLifecycleOwner.lifecycleScope.launch {
            selected = dataStore.getTimeFormat()
            renderTimeFormatSelection(binding, selected)
            binding.btn12Hour.setOnClickListener {
                selected = TimeFormat.HOUR_12
                renderTimeFormatSelection(binding, selected)
            }
            binding.btn12HourNs.setOnClickListener {
                selected = TimeFormat.HOUR_12_NS
                renderTimeFormatSelection(binding, selected)
            }
            binding.btn24Hour.setOnClickListener {
                selected = TimeFormat.HOUR_24
                renderTimeFormatSelection(binding, selected)
            }
            binding.btnFloating.setOnClickListener {
                selected = TimeFormat.FLOATING
                renderTimeFormatSelection(binding, selected)
            }
            binding.buttonDone.setOnClickListener {
                fragment.viewLifecycleOwner.lifecycleScope.launch {
                    dataStore.setTimeFormat(selected)
                    onSaved(selected)
                }
                dialog.dismiss()
            }
            binding.buttonCancel.setOnClickListener { dialog.dismiss() }
            dialog.show()
        }
    }

    fun showPrayerAngleDialog(
        fragment: Fragment,
        title: String,
        items: List<Double>,
        defaultSelection: Double,
        onSaved: (Double) -> Unit
    ) {
        val formattedItems = items.map { "$it°" }.toTypedArray()
        val defaultIndex = items.indexOf(defaultSelection).takeIf { it >= 0 } ?: 0
        AlertDialog.Builder(fragment.requireContext())
            .setTitle(title)
            .setSingleChoiceItems(formattedItems, defaultIndex) { dialog, which ->
                onSaved(items[which])
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    fun showCorrectionMinutesDialog(
        fragment: Fragment,
        title: String,
        defaultMinute: Int,
        onSaved: (Int) -> Unit
    ) {
        val items = (-59..59).toList()
        val labels = items.map { "$it ${fragment.getString(R.string.minute)}" }.toTypedArray()
        val selectedIndex = items.indexOf(defaultMinute).takeIf { it >= 0 } ?: items.indexOf(0)
        AlertDialog.Builder(fragment.requireContext())
            .setTitle(title)
            .setSingleChoiceItems(labels, selectedIndex) { dialog, which ->
                onSaved(items[which])
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun renderAsrSelection(binding: DialogAsrSettingsBinding, selected: AsrJuristicMethod) {
        renderToggle(binding.btnHanafi, selected == AsrJuristicMethod.HANAFI)
        renderToggle(binding.btnShafi, selected == AsrJuristicMethod.SHAFI)
    }

    private fun renderHighLatSelection(
        binding: DialogPrayerHighLatBinding,
        selected: HighLatitudeAdjustment
    ) {
        renderToggle(binding.btnNoAdjustment, selected == HighLatitudeAdjustment.NO_ADJUSTMENT)
        renderToggle(binding.btnMiddleOfNight, selected == HighLatitudeAdjustment.MID_NIGHT)
        renderToggle(binding.btnSeventhOfNight, selected == HighLatitudeAdjustment.ONE_SEVENTH)
        renderToggle(binding.btnTwilightAngle, selected == HighLatitudeAdjustment.TWILIGHT_ANGLE)
    }

    private fun renderTimeFormatSelection(
        binding: DialogPrayerTimeFormatBinding,
        selected: TimeFormat
    ) {
        renderToggle(binding.btn12Hour, selected == TimeFormat.HOUR_12)
        renderToggle(binding.btn12HourNs, selected == TimeFormat.HOUR_12_NS)
        renderToggle(binding.btn24Hour, selected == TimeFormat.HOUR_24)
        renderToggle(binding.btnFloating, selected == TimeFormat.FLOATING)
    }

    private fun renderToggle(view: android.widget.TextView, selected: Boolean) {
        val context = view.context
        if (selected) {
            view.setBackgroundResource(R.drawable.bg_btn_prayer_setting_on)
            view.setTextColor(context.getColor(R.color.white))
        } else {
            view.setBackgroundResource(R.drawable.bg_btn_prayer_setting_off)
            view.setTextColor(context.getColor(R.color.black))
        }
    }
}
