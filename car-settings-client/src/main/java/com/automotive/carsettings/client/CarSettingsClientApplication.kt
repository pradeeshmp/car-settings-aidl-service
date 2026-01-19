package com.automotive.carsettings.client

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Application class for Car Settings Client with Hilt dependency injection.
 */
@HiltAndroidApp
class CarSettingsClientApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Timber.d("CarSettingsClientApplication initialized")
    }
}
