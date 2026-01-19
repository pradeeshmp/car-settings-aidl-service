package com.example.carsettings.common

import com.example.carsettings.aidl.PropertyValue
import com.example.carsettings.constants.PropertyConstants

fun PropertyValue.toInt(): Int = intValue
fun PropertyValue.toFloat(): Float = floatValue
fun PropertyValue.toBoolean(): Boolean = booleanValue
fun PropertyValue.toStringValue(): String = stringValue ?: ""

fun Int.toPropertyValue(propertyId: Int, areaId: Int, status: Int = 0): PropertyValue {
    return PropertyValue().apply {
        this.propertyId = propertyId
        this.areaId = areaId
        this.type = PropertyConstants.TYPE_INT32
        this.status = status
        this.timestampNanos = System.nanoTime()
        this.intValue = this@toPropertyValue
    }
}

fun Float.toPropertyValue(propertyId: Int, areaId: Int, status: Int = 0): PropertyValue {
    return PropertyValue().apply {
        this.propertyId = propertyId
        this.areaId = areaId
        this.type = PropertyConstants.TYPE_FLOAT
        this.status = status
        this.timestampNanos = System.nanoTime()
        this.floatValue = this@toPropertyValue
    }
}

fun Boolean.toPropertyValue(propertyId: Int, areaId: Int, status: Int = 0): PropertyValue {
    return PropertyValue().apply {
        this.propertyId = propertyId
        this.areaId = areaId
        this.type = PropertyConstants.TYPE_BOOLEAN
        this.status = status
        this.timestampNanos = System.nanoTime()
        this.booleanValue = this@toPropertyValue
    }
}

fun String.toPropertyValue(propertyId: Int, areaId: Int, status: Int = 0): PropertyValue {
    return PropertyValue().apply {
        this.propertyId = propertyId
        this.areaId = areaId
        this.type = PropertyConstants.TYPE_STRING
        this.status = status
        this.timestampNanos = System.nanoTime()
        this.stringValue = this@toPropertyValue
    }
}
