package com.automotive.carsettings.aidl

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents an error that occurred during a property operation.
 */
@Parcelize
data class CarSettingsError(
    val errorCode: Int,
    val message: String,
    val propertyId: Int = 0,
    val areaId: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable {

    companion object {
        const val ERROR_SUCCESS = 0
        const val ERROR_PROPERTY_NOT_FOUND = 1
        const val ERROR_INVALID_VALUE = 2
        const val ERROR_PERMISSION_DENIED = 3
        const val ERROR_SERVICE_NOT_READY = 4
        const val ERROR_AREA_NOT_SUPPORTED = 5
        const val ERROR_OPERATION_FAILED = 6
        const val ERROR_TIMEOUT = 7
        const val ERROR_NOT_AVAILABLE = 8
        const val ERROR_INTERNAL = 9
    }
}
