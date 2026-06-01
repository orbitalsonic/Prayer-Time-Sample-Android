package com.orbitalsonic.prayertimesample

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.orbitalsonic.prayertimesample.data.notification.PrayerNotificationHelper
import com.orbitalsonic.prayertimesample.domain.model.PrayerName
import com.orbitalsonic.prayertimesample.domain.model.PrayerNotificationMode
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PrayerNotificationFlowTest {

    @Test
    fun showPrayerNotification_doesNotCrash() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        PrayerNotificationHelper.ensureChannels(context)
        PrayerNotificationHelper.showPrayerNotification(
            context,
            PrayerName.DHUHR,
            "12:30",
            PrayerNotificationMode.NOTIFICATION_ONLY
        )
        assertEquals(
            "com.orbitalsonic.prayertimesample",
            context.packageName
        )
    }
}
