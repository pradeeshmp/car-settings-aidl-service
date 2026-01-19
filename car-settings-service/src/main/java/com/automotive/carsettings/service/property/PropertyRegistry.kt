package com.automotive.carsettings.service.property

import com.automotive.carsettings.aidl.*
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Registry of property metadata and configurations.
 */
@Singleton
class PropertyRegistry @Inject constructor() {

    private val propertyInfoMap = ConcurrentHashMap<Int, CarPropertyInfo>()
    private val propertyConfigMap = ConcurrentHashMap<Int, CarPropertyConfig>()

    init {
        registerAllProperties()
    }

    fun getAllPropertyIds(): IntArray {
        return PropertyConstants.getAllPropertyIds()
    }

    fun getPropertyInfo(propertyId: Int): CarPropertyInfo? {
        return propertyInfoMap[propertyId]
    }

    fun getPropertyConfig(propertyId: Int): CarPropertyConfig? {
        return propertyConfigMap[propertyId]
    }

    fun isPropertyAvailable(propertyId: Int, areaId: Int): Boolean {
        val config = propertyConfigMap[propertyId] ?: return false
        return areaId in config.supportedAreas
    }

    fun getSupportedAreas(propertyId: Int): IntArray {
        return propertyConfigMap[propertyId]?.supportedAreas ?: intArrayOf()
    }

    private fun registerAllProperties() {
        // Display properties
        registerGlobalProperty(
            PropertyConstants.DISPLAY_BRIGHTNESS,
            "Display Brightness",
            "Screen brightness level (0-255)",
            CarPropertyInfo.TYPE_INT32,
            CarPropertyInfo.ACCESS_MODE_READ_WRITE,
            CarPropertyInfo.PERMISSION_NORMAL
        )

        registerGlobalProperty(
            PropertyConstants.DISPLAY_THEME,
            "Display Theme",
            "UI theme (0=light, 1=dark, 2=auto)",
            CarPropertyInfo.TYPE_INT32,
            CarPropertyInfo.ACCESS_MODE_READ_WRITE,
            CarPropertyInfo.PERMISSION_NORMAL
        )

        registerGlobalProperty(
            PropertyConstants.DISPLAY_LANGUAGE,
            "Display Language",
            "System language (locale string)",
            CarPropertyInfo.TYPE_STRING,
            CarPropertyInfo.ACCESS_MODE_READ_WRITE,
            CarPropertyInfo.PERMISSION_NORMAL
        )

        // Audio properties
        registerGlobalProperty(
            PropertyConstants.AUDIO_VOLUME_MEDIA,
            "Media Volume",
            "Media audio volume (0-30)",
            CarPropertyInfo.TYPE_INT32,
            CarPropertyInfo.ACCESS_MODE_READ_WRITE,
            CarPropertyInfo.PERMISSION_NORMAL
        )

        registerGlobalProperty(
            PropertyConstants.AUDIO_BALANCE,
            "Audio Balance",
            "Left/Right audio balance (-10 to 10)",
            CarPropertyInfo.TYPE_INT32,
            CarPropertyInfo.ACCESS_MODE_READ_WRITE,
            CarPropertyInfo.PERMISSION_NORMAL
        )

        // HVAC properties (seat-specific)
        registerSeatProperty(
            PropertyConstants.HVAC_TEMPERATURE,
            "HVAC Temperature",
            "Temperature setpoint in Celsius (16.0-30.0)",
            CarPropertyInfo.TYPE_FLOAT,
            CarPropertyInfo.ACCESS_MODE_READ_WRITE,
            CarPropertyInfo.PERMISSION_NORMAL,
            intArrayOf(AreaConstants.SEAT_DRIVER, AreaConstants.SEAT_PASSENGER)
        )

        registerSeatProperty(
            PropertyConstants.HVAC_FAN_SPEED,
            "HVAC Fan Speed",
            "Fan speed level (0-7)",
            CarPropertyInfo.TYPE_INT32,
            CarPropertyInfo.ACCESS_MODE_READ_WRITE,
            CarPropertyInfo.PERMISSION_NORMAL,
            intArrayOf(AreaConstants.SEAT_DRIVER, AreaConstants.SEAT_PASSENGER)
        )

        registerGlobalProperty(
            PropertyConstants.HVAC_AC_ON,
            "HVAC AC",
            "Air conditioning on/off",
            CarPropertyInfo.TYPE_BOOLEAN,
            CarPropertyInfo.ACCESS_MODE_READ_WRITE,
            CarPropertyInfo.PERMISSION_NORMAL
        )

        // Vehicle properties
        registerGlobalProperty(
            PropertyConstants.VEHICLE_HEADLIGHTS_AUTO,
            "Auto Headlights",
            "Automatic headlight control",
            CarPropertyInfo.TYPE_BOOLEAN,
            CarPropertyInfo.ACCESS_MODE_READ_WRITE,
            CarPropertyInfo.PERMISSION_PRIVILEGED
        )

        // Privacy properties
        registerGlobalProperty(
            PropertyConstants.PRIVACY_LOCATION_ENABLED,
            "Location Services",
            "Location services enabled",
            CarPropertyInfo.TYPE_BOOLEAN,
            CarPropertyInfo.ACCESS_MODE_READ_WRITE,
            CarPropertyInfo.PERMISSION_NORMAL
        )

        // Add more properties as needed...
    }

    private fun registerGlobalProperty(
        propertyId: Int,
        name: String,
        description: String,
        type: Int,
        accessMode: Int,
        permissionLevel: Int
    ) {
        val info = CarPropertyInfo(
            id = propertyId,
            name = name,
            description = description,
            type = type,
            areaType = CarPropertyInfo.AREA_TYPE_GLOBAL,
            accessMode = accessMode,
            permissionLevel = permissionLevel,
            changeMode = CarPropertyInfo.CHANGE_MODE_ON_CHANGE
        )

        val config = CarPropertyConfig(
            propertyId = propertyId,
            type = type,
            supportedAreas = intArrayOf(AreaConstants.AREA_GLOBAL),
            areaConfigs = listOf(AreaConfig(AreaConstants.AREA_GLOBAL))
        )

        propertyInfoMap[propertyId] = info
        propertyConfigMap[propertyId] = config
    }

    private fun registerSeatProperty(
        propertyId: Int,
        name: String,
        description: String,
        type: Int,
        accessMode: Int,
        permissionLevel: Int,
        supportedSeats: IntArray
    ) {
        val info = CarPropertyInfo(
            id = propertyId,
            name = name,
            description = description,
            type = type,
            areaType = CarPropertyInfo.AREA_TYPE_SEAT,
            accessMode = accessMode,
            permissionLevel = permissionLevel,
            changeMode = CarPropertyInfo.CHANGE_MODE_ON_CHANGE
        )

        val config = CarPropertyConfig(
            propertyId = propertyId,
            type = type,
            supportedAreas = supportedSeats,
            areaConfigs = supportedSeats.map { AreaConfig(it) }
        )

        propertyInfoMap[propertyId] = info
        propertyConfigMap[propertyId] = config
    }
}
