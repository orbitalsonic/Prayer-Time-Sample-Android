package com.orbitalsonic.prayertimesample.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.orbitalsonic.prayertimesample.PrayerTimeApp
class PrayerRescheduleWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as PrayerTimeApp).container
        return runCatching {
            container.refreshPrayerTimesUseCase()
            Result.success()
        }.getOrElse { Result.retry() }
    }

}
