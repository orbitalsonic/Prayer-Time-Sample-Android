package com.orbitalsonic.prayertimesample.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.orbitalsonic.prayertimesample.worker.PrayerRescheduleWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class TimeChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> {
                WorkManager.getInstance(context).enqueue(
                    OneTimeWorkRequestBuilder<PrayerRescheduleWorker>().build()
                )
            }
        }
    }
}
