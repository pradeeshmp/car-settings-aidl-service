package com.example.carsettings.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class CarSettingsService : Service() {

    @Inject
    lateinit var serviceImpl: CarSettingsServiceImpl

    override fun onCreate() {
        super.onCreate()
        Timber.i("CarSettingsService created")
    }

    override fun onBind(intent: Intent): IBinder? {
        Timber.i("CarSettingsService bound with intent: $intent")
        return serviceImpl
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Timber.i("CarSettingsService unbound")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        Timber.i("CarSettingsService destroyed")
        super.onDestroy()
    }
}
