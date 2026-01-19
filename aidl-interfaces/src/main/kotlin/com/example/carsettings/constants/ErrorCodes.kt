package com.example.carsettings.constants

object ErrorCodes {
    const val SUCCESS = 0
    const val ERROR_UNKNOWN = -1
    const val ERROR_INVALID_ARGUMENT = -2
    const val ERROR_PROPERTY_NOT_FOUND = -3
    const val ERROR_AREA_NOT_SUPPORTED = -4
    const val ERROR_PERMISSION_DENIED = -5
    const val ERROR_SERVICE_NOT_READY = -6
    const val ERROR_LIMIT_EXCEEDED = -7
    const val ERROR_TIMEOUT = -8

    fun getErrorMessage(errorCode: Int): String {
        return when (errorCode) {
            SUCCESS -> "Success"
            ERROR_INVALID_ARGUMENT -> "Invalid argument"
            ERROR_PROPERTY_NOT_FOUND -> "Property not found"
            ERROR_AREA_NOT_SUPPORTED -> "Area not supported"
            ERROR_PERMISSION_DENIED -> "Permission denied"
            else -> "Unknown error ($errorCode)"
        }
    }
}
