package com.orbitalsonic.prayertimesample

import android.app.Application
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.orbitalsonic.prayertimesample.data.notification.PrayerNotificationHelper
import com.orbitalsonic.prayertimesample.di.AppContainer
import com.orbitalsonic.prayertimesample.worker.PrayerRescheduleWorker

class PrayerTimeApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        PrayerNotificationHelper.ensureChannels(this)
        WorkManager.getInstance(this).enqueue(
            OneTimeWorkRequestBuilder<PrayerRescheduleWorker>().build()
        )
    }
}
