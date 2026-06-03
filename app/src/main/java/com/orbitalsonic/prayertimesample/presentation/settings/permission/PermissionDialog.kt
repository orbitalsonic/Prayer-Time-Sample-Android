package com.orbitalsonic.prayertimesample.presentation.settings.permission

import android.app.AlertDialog
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.orbitalsonic.prayertimesample.databinding.DialogPermissionBinding

data class PermissionDialogConfig(
    @StringRes val titleRes: Int,
    @StringRes val messageRes: Int,
    @DrawableRes val iconRes: Int
)

object PermissionDialog {

    fun show(
        fragment: Fragment,
        config: PermissionDialogConfig,
        onOpenSettings: () -> Unit,
        onCancel: (() -> Unit)? = null
    ) {
        val activity = fragment.activity ?: return
        val binding = DialogPermissionBinding.inflate(activity.layoutInflater)
        binding.permissionDialogIcon.setImageResource(config.iconRes)
        binding.permissionDialogTitle.setText(config.titleRes)
        binding.permissionDialogMessage.setText(config.messageRes)

        val dialog = AlertDialog.Builder(activity)
            .setView(binding.root)
            .setCancelable(true)
            .create()

        binding.btnPermissionOpenSettings.setOnClickListener {
            onOpenSettings()
            dialog.dismiss()
        }
        binding.btnPermissionCancel.setOnClickListener {
            onCancel?.invoke()
            dialog.dismiss()
        }
        dialog.show()
    }
}
