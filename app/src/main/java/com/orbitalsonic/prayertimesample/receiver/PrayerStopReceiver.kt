package com.orbitalsonic.prayertimesample.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.orbitalsonic.prayertimesample.PrayerTimeApp

class PrayerStopReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_STOP_AZAN) return
        val app = context.applicationContext as PrayerTimeApp
        app.container.azanPlayerManager.stop()
    }

    companion object {
        const val ACTION_STOP_AZAN = "com.orbitalsonic.prayertimesample.STOP_AZAN"
    }
}
