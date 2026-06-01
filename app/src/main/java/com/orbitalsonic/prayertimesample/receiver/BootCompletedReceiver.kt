package com.orbitalsonic.prayertimesample.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.orbitalsonic.prayertimesample.PrayerTimeApp
import com.orbitalsonic.prayertimesample.worker.PrayerRescheduleWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_LOCKED_BOOT_COMPLETED
        ) {
            return
        }
        WorkManager.getInstance(context).enqueue(
            OneTimeWorkRequestBuilder<PrayerRescheduleWorker>().build()
        )
    }
}
