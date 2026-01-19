package com.automotive.carsettings.aidl

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Metadata about a car property including its type, access permissions, and behavior.
 */
@Parcelize
data class CarPropertyInfo(
    val id: Int,
    val name: String,
    val description: String,
    val type: Int,
    val areaType: Int,
    val accessMode: Int,
    val permissionLevel: Int,
    val changeMode: Int
) : Parcelable {

    companion object {
        // Property types
        const val TYPE_BOOLEAN = 0
        const val TYPE_INT32 = 1
        const val TYPE_FLOAT = 2
        const val TYPE_STRING = 3
        const val TYPE_INT32_VEC = 4
        const val TYPE_FLOAT_VEC = 5

        // Area types
        const val AREA_TYPE_GLOBAL = 0
        const val AREA_TYPE_SEAT = 1
        const val AREA_TYPE_DOOR = 2
        const val AREA_TYPE_WINDOW = 3
        const val AREA_TYPE_MIRROR = 4
        const val AREA_TYPE_WHEEL = 5

        // Access modes
        const val ACCESS_MODE_READ = 1
        const val ACCESS_MODE_WRITE = 2
        const val ACCESS_MODE_READ_WRITE = 3

        // Permission levels
        const val PERMISSION_NORMAL = 0
        const val PERMISSION_PRIVILEGED = 1
        const val PERMISSION_SYSTEM = 2

        // Change modes
        const val CHANGE_MODE_STATIC = 0
        const val CHANGE_MODE_ON_CHANGE = 1
        const val CHANGE_MODE_CONTINUOUS = 2
    }
}
