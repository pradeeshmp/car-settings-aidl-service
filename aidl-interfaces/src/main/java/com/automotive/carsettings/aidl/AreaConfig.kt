package com.automotive.carsettings.aidl

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Configuration for a specific area of a property, including value constraints.
 */
@Parcelize
data class AreaConfig(
    val areaId: Int,
    val minIntValue: Int = Int.MIN_VALUE,
    val maxIntValue: Int = Int.MAX_VALUE,
    val minFloatValue: Float = Float.MIN_VALUE,
    val maxFloatValue: Float = Float.MAX_VALUE
) : Parcelable
