package com.automotive.carsettings.service.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.automotive.carsettings.service.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the foreground service notification.
 */
@Singleton
class ServiceNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "car_settings_service"
        private const val CHANNEL_NAME = "Car Settings Service"
    }

    init {
        createNotificationChannel()
    }

    /**
     * Create the foreground service notification.
     */
    fun createNotification(): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Car Settings Service")
            .setContentText("Managing vehicle settings")
            .setSmallIcon(android.R.drawable.ic_menu_preferences)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notification for Car Settings background service"
                setShowBadge(false)
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
