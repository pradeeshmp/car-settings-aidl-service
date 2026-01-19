package com.automotive.carsettings.aidl

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Configuration for a car property including supported areas and constraints.
 */
@Parcelize
data class CarPropertyConfig(
    val propertyId: Int,
    val type: Int,
    val supportedAreas: IntArray,
    val areaConfigs: List<AreaConfig>
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CarPropertyConfig

        if (propertyId != other.propertyId) return false
        if (type != other.type) return false
        if (!supportedAreas.contentEquals(other.supportedAreas)) return false
        if (areaConfigs != other.areaConfigs) return false

        return true
    }

    override fun hashCode(): Int {
        var result = propertyId
        result = 31 * result + type
        result = 31 * result + supportedAreas.contentHashCode()
        result = 31 * result + areaConfigs.hashCode()
        return result
    }
}
