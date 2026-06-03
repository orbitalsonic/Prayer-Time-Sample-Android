package com.orbitalsonic.prayertimesample.presentation.prayer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.orbitalsonic.prayertimesample.R
import com.orbitalsonic.prayertimesample.databinding.ItemPrayerTimeBinding
import com.orbitalsonic.prayertimesample.domain.model.PrayerName
import com.orbitalsonic.prayertimesample.presentation.prayer.model.PrayerUiModel

class PrayerListAdapter(
    private val onNotificationModeClick: (PrayerName) -> Unit
) : ListAdapter<PrayerUiModel, PrayerListAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPrayerTimeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onNotificationModeClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemPrayerTimeBinding,
        private val onNotificationModeClick: (PrayerName) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PrayerUiModel) {
            binding.prayerIcon.setImageResource(item.iconRes)
            binding.prayerName.text = item.prayerName
            binding.prayerTime.text = item.prayerTime
            binding.btnNotificationMode.setImageResource(item.notificationMode.iconRes())

            val color = when {
                item.isNow -> R.color.prayer_now
                item.isPassed -> R.color.prayer_passed
                else -> R.color.prayer_default
            }
            val colorInt = ContextCompat.getColor(binding.root.context, color)
            binding.prayerName.setTextColor(colorInt)
            binding.prayerTime.setTextColor(colorInt)
            val background = when {
                item.isNow -> R.drawable.bg_prayer_now
                else -> R.drawable.bg_prayer
            }
            binding.itemPrayer.background = ContextCompat.getDrawable(binding.root.context, background)

            binding.btnNotificationMode.setOnClickListener {
                onNotificationModeClick(item.prayer)
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<PrayerUiModel>() {
        override fun areItemsTheSame(oldItem: PrayerUiModel, newItem: PrayerUiModel) =
            oldItem.prayer == newItem.prayer

        override fun areContentsTheSame(oldItem: PrayerUiModel, newItem: PrayerUiModel) =
            oldItem == newItem
    }
}