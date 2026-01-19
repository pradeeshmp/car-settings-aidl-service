package com.automotive.carsettings.service

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Application class for Car Settings Service with Hilt dependency injection.
 */
@HiltAndroidApp
class CarSettingsApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Timber.d("CarSettingsApplication initialized")
    }
}
