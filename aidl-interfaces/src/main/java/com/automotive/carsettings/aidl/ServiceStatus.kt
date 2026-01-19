package com.automotive.carsettings.aidl

/**
 * Service status codes for ICarSettingsCallback.onServiceStatusChanged()
 */
object ServiceStatus {
    const val STATUS_INITIALIZING = 0
    const val STATUS_READY = 1
    const val STATUS_SHUTTING_DOWN = 2
    const val STATUS_ERROR = 3
    const val STATUS_SUSPENDED = 4
}
