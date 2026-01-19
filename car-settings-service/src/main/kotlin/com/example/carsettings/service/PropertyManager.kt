package com.example.carsettings.service

import com.example.carsettings.aidl.PropertyValue
import com.example.carsettings.aidl.CarPropertyConfig
import com.example.carsettings.aidl.AreaConfig
import com.example.carsettings.constants.PropertyConstants
import com.example.carsettings.constants.AreaConstants
import com.example.carsettings.constants.PropertyType
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

@Singleton
class PropertyManager @Inject constructor() {

    private val propertyConfigs = ConcurrentHashMap<Int, CarPropertyConfig>()
    private val propertyValues = ConcurrentHashMap<String, PropertyValue>()

    init {
        initializeConfigs()
        initializeDefaultValues()
    }

    private fun initializeConfigs() {
        // Display Brightness
        addConfig(CarPropertyConfig().apply {
            propertyId = PropertyConstants.DISPLAY_BRIGHTNESS
            type = PropertyType.INT32
            supportedAreas = intArrayOf(AreaConstants.AREA_GLOBAL)
            areaConfigs = arrayOf(AreaConfig().apply {
                areaId = AreaConstants.AREA_GLOBAL
                minInt = 0
                maxInt = 100
            })
        })

        // HVAC Temperature
        addConfig(CarPropertyConfig().apply {
            propertyId = PropertyConstants.HVAC_TEMPERATURE_SET
            type = PropertyType.FLOAT
            supportedAreas = intArrayOf(AreaConstants.SEAT_DRIVER, AreaConstants.SEAT_PASSENGER)
            areaConfigs = arrayOf(
                AreaConfig().apply {
                    areaId = AreaConstants.SEAT_DRIVER
                    minFloat = 16.0f
                    maxFloat = 28.0f
                },
                AreaConfig().apply {
                    areaId = AreaConstants.SEAT_PASSENGER
                    minFloat = 16.0f
                    maxFloat = 28.0f
                }
            )
        })

        // Night Mode
        addConfig(CarPropertyConfig().apply {
            propertyId = PropertyConstants.DISPLAY_NIGHT_MODE
            type = PropertyType.BOOLEAN
            supportedAreas = intArrayOf(AreaConstants.AREA_GLOBAL)
        })
    }

    private fun addConfig(config: CarPropertyConfig) {
        propertyConfigs[config.propertyId] = config
    }

    private fun initializeDefaultValues() {
        updateValue(PropertyValue().apply {
            propertyId = PropertyConstants.DISPLAY_BRIGHTNESS
            areaId = AreaConstants.AREA_GLOBAL
            type = PropertyType.INT32
            intValue = 50
            timestampNanos = System.nanoTime()
        })

        updateValue(PropertyValue().apply {
            propertyId = PropertyConstants.HVAC_TEMPERATURE_SET
            areaId = AreaConstants.SEAT_DRIVER
            type = PropertyType.FLOAT
            floatValue = 22.0f
            timestampNanos = System.nanoTime()
        })

        updateValue(PropertyValue().apply {
            propertyId = PropertyConstants.HVAC_TEMPERATURE_SET
            areaId = AreaConstants.SEAT_PASSENGER
            type = PropertyType.FLOAT
            floatValue = 22.0f
            timestampNanos = System.nanoTime()
        })

        updateValue(PropertyValue().apply {
            propertyId = PropertyConstants.DISPLAY_NIGHT_MODE
            areaId = AreaConstants.AREA_GLOBAL
            type = PropertyType.BOOLEAN
            booleanValue = false
            timestampNanos = System.nanoTime()
        })
    }

    fun getPropertyConfig(propertyId: Int): CarPropertyConfig? = propertyConfigs[propertyId]

    fun getPropertyIds(): IntArray = propertyConfigs.keys().toList().toIntArray()

    fun getPropertyValue(propertyId: Int, areaId: Int): PropertyValue? {
        val key = generateKey(propertyId, areaId)
        return propertyValues[key]
    }

    fun updateValue(value: PropertyValue) {
        val key = generateKey(value.propertyId, value.areaId)
        value.timestampNanos = System.nanoTime()
        propertyValues[key] = value
        Timber.d("Updated property ${PropertyConstants.getPropertyName(value.propertyId)} at ${value.areaId} to $value")
    }

    fun isPropertyAvailable(propertyId: Int, areaId: Int): Boolean {
        val config = propertyConfigs[propertyId] ?: return false
        return config.supportedAreas.contains(areaId)
    }

    private fun generateKey(propertyId: Int, areaId: Int): String = "$propertyId:$areaId"
}
