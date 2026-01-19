package com.automotive.carsettings.aidl

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents a vehicle property value with metadata.
 * Mirrors the structure of CarPropertyValue in Android Automotive.
 */
@Parcelize
data class PropertyValue(
    val propertyId: Int = 0,
    val areaId: Int = 0,
    val status: Int = STATUS_AVAILABLE,
    val timestampNanos: Long = System.nanoTime(),
    val intValue: Int = 0,
    val floatValue: Float = 0f,
    val stringValue: String = "",
    val booleanValue: Boolean = false,
    val intArray: IntArray = intArrayOf(),
    val floatArray: FloatArray = floatArrayOf()
) : Parcelable {

    companion object {
        const val STATUS_AVAILABLE = 0
        const val STATUS_UNAVAILABLE = 1
        const val STATUS_ERROR = 2

        fun forInt(propertyId: Int, areaId: Int, value: Int): PropertyValue {
            return PropertyValue(
                propertyId = propertyId,
                areaId = areaId,
                intValue = value
            )
        }

        fun forFloat(propertyId: Int, areaId: Int, value: Float): PropertyValue {
            return PropertyValue(
                propertyId = propertyId,
                areaId = areaId,
                floatValue = value
            )
        }

        fun forBoolean(propertyId: Int, areaId: Int, value: Boolean): PropertyValue {
            return PropertyValue(
                propertyId = propertyId,
                areaId = areaId,
                booleanValue = value
            )
        }

        fun forString(propertyId: Int, areaId: Int, value: String): PropertyValue {
            return PropertyValue(
                propertyId = propertyId,
                areaId = areaId,
                stringValue = value
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PropertyValue

        if (propertyId != other.propertyId) return false
        if (areaId != other.areaId) return false
        if (status != other.status) return false
        if (intValue != other.intValue) return false
        if (floatValue != other.floatValue) return false
        if (stringValue != other.stringValue) return false
        if (booleanValue != other.booleanValue) return false
        if (!intArray.contentEquals(other.intArray)) return false
        if (!floatArray.contentEquals(other.floatArray)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = propertyId
        result = 31 * result + areaId
        result = 31 * result + status
        result = 31 * result + intValue
        result = 31 * result + floatValue.hashCode()
        result = 31 * result + stringValue.hashCode()
        result = 31 * result + booleanValue.hashCode()
        result = 31 * result + intArray.contentHashCode()
        result = 31 * result + floatArray.contentHashCode()
        return result
    }
}
