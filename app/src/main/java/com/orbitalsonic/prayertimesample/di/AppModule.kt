package com.orbitalsonic.prayertimesample.di

import android.app.Application
import com.orbitalsonic.prayertimesample.PrayerTimeApp

/**
 * Manual DI entry point. [AppContainer] wires Presentation → Domain → Data layers.
 * Replace with Hilt/Koin modules when the app grows.
 */
object AppModule {

    fun provideContainer(application: Application): AppContainer {
        require(application is PrayerTimeApp) {
            "Application must be PrayerTimeApp"
        }
        return application.container
    }
}
