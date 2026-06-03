package com.orbitalsonic.prayertimesample.presentation.settings.adapters

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.orbitalsonic.prayertimesample.databinding.ItemPermissionBinding
import com.orbitalsonic.prayertimesample.presentation.settings.model.PermissionItem

class PermissionListAdapter(
    private val onPermissionSwitchTapped: (PermissionItem) -> Unit
) : ListAdapter<PermissionItem, PermissionListAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPermissionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onPermissionSwitchTapped)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemPermissionBinding,
        private val onPermissionSwitchTapped: (PermissionItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PermissionItem) {
            binding.permissionTitle.setText(item.titleRes)
            binding.permissionSubtitle.setText(item.subtitleRes)
            binding.permissionSwitch.setOnCheckedChangeListener(null)
            binding.permissionSwitch.isChecked = item.isGranted
            binding.permissionSwitch.jumpDrawablesToCurrentState()
            binding.permissionSwitch.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    onPermissionSwitchTapped(item)
                }
                true
            }
            binding.root.setOnClickListener { onPermissionSwitchTapped(item) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<PermissionItem>() {
        override fun areItemsTheSame(oldItem: PermissionItem, newItem: PermissionItem) =
            oldItem.type == newItem.type

        override fun areContentsTheSame(oldItem: PermissionItem, newItem: PermissionItem) =
            oldItem == newItem
    }
}
