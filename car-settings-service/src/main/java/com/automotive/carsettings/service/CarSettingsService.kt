package com.automotive.carsettings.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.automotive.carsettings.service.notification.ServiceNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * Main Android Service entry point for the Car Settings Service.
 * Runs as a foreground service to ensure reliability.
 */
@AndroidEntryPoint
class CarSettingsService : Service() {

    @Inject
    lateinit var serviceImpl: CarSettingsServiceImpl

    @Inject
    lateinit var notificationManager: ServiceNotificationManager

    private var clientCount = 0

    override fun onCreate() {
        super.onCreate()
        Timber.d("CarSettingsService onCreate()")

        // Start as foreground service
        val notification = notificationManager.createNotification()
        startForeground(ServiceNotificationManager.NOTIFICATION_ID, notification)

        // Initialize service implementation
        serviceImpl.initialize()
    }

    override fun onBind(intent: Intent?): IBinder? {
        Timber.d("CarSettingsService onBind() - clientCount: ${clientCount + 1}")
        clientCount++
        return serviceImpl
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Timber.d("CarSettingsService onUnbind() - clientCount: ${clientCount - 1}")
        clientCount--

        // Return true to receive onRebind
        return true
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        Timber.d("CarSettingsService onRebind() - clientCount: ${clientCount + 1}")
        clientCount++
    }

    override fun onDestroy() {
        Timber.d("CarSettingsService onDestroy()")
        serviceImpl.shutdown()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("CarSettingsService onStartCommand()")
        return START_STICKY
    }
}
