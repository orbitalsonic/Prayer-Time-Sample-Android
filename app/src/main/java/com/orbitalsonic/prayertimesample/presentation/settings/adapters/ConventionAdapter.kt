package com.orbitalsonic.prayertimesample.presentation.settings.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.orbitalsonic.prayertimesample.databinding.ItemPrayerConventionBinding
import com.orbitalsonic.prayertimesample.presentation.settings.model.ConventionItem

class ConventionAdapter(
    private val onClick: (ConventionItem) -> Unit
) : ListAdapter<ConventionItem, ConventionAdapter.ConventionViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConventionViewHolder {
        val binding = ItemPrayerConventionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ConventionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ConventionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ConventionViewHolder(
        private val binding: ItemPrayerConventionBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ConventionItem) = with(binding) {
            title.text = item.title
            subtitle.text = item.subtitle
            radioButton.isChecked = item.isSelected
            root.setOnClickListener { onClick(item) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<ConventionItem>() {
        override fun areItemsTheSame(oldItem: ConventionItem, newItem: ConventionItem): Boolean =
            oldItem.convention == newItem.convention

        override fun areContentsTheSame(oldItem: ConventionItem, newItem: ConventionItem): Boolean =
            oldItem == newItem
    }
}