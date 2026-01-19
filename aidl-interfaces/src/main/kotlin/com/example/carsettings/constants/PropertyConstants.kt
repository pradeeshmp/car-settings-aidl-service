package com.example.carsettings.constants

object PropertyConstants {
    // Property Type Masks
    const val TYPE_MASK = 0x00ff0000
    const val TYPE_INT32 = 0x00400000
    const val TYPE_INT32_VEC = 0x00410000
    const val TYPE_FLOAT = 0x00600000
    const val TYPE_FLOAT_VEC = 0x00610000
    const val TYPE_STRING = 0x00100000
    const val TYPE_BOOLEAN = 0x00200000
    const val TYPE_BYTES = 0x00700000

    // Area Type Masks
    const val AREA_MASK = 0x0f000000
    const val AREA_GLOBAL = 0x01000000
    const val AREA_SEAT = 0x02000000
    const val AREA_DOOR = 0x03000000
    const val AREA_WINDOW = 0x04000000
    const val AREA_MIRROR = 0x05000000

    // Property Groups
    const val GROUP_DISPLAY = 0x00010000
    const val GROUP_AUDIO = 0x00020000
    const val GROUP_HVAC = 0x00030000
    const val GROUP_VEHICLE = 0x00040000
    const val GROUP_PROFILE = 0x00050000
    const val GROUP_PRIVACY = 0x00060000

    // Display Properties
    const val DISPLAY_BRIGHTNESS = 0x1101 + GROUP_DISPLAY + TYPE_INT32 + AREA_GLOBAL
    const val DISPLAY_THEME = 0x1102 + GROUP_DISPLAY + TYPE_INT32 + AREA_GLOBAL // 0: Light, 1: Dark, 2: Auto
    const val DISPLAY_LANGUAGE = 0x1103 + GROUP_DISPLAY + TYPE_STRING + AREA_GLOBAL
    const val DISPLAY_NIGHT_MODE = 0x1104 + GROUP_DISPLAY + TYPE_BOOLEAN + AREA_GLOBAL

    // Audio Properties
    const val AUDIO_VOLUME_MEDIA = 0x1201 + GROUP_AUDIO + TYPE_INT32 + AREA_GLOBAL
    const val AUDIO_VOLUME_NAV = 0x1202 + GROUP_AUDIO + TYPE_INT32 + AREA_GLOBAL
    const val AUDIO_VOLUME_CALL = 0x1203 + GROUP_AUDIO + TYPE_INT32 + AREA_GLOBAL
    const val AUDIO_VOLUME_VOICE = 0x1204 + GROUP_AUDIO + TYPE_INT32 + AREA_GLOBAL
    const val AUDIO_BALANCE = 0x1205 + GROUP_AUDIO + TYPE_FLOAT + AREA_GLOBAL // -1.0 to 1.0
    const val AUDIO_FADE = 0x1206 + GROUP_AUDIO + TYPE_FLOAT + AREA_GLOBAL // -1.0 to 1.0
    const val AUDIO_EQ_BASS = 0x1207 + GROUP_AUDIO + TYPE_INT32 + AREA_GLOBAL
    const val AUDIO_EQ_TREBLE = 0x1208 + GROUP_AUDIO + TYPE_INT32 + AREA_GLOBAL

    // HVAC Properties
    const val HVAC_TEMPERATURE_SET = 0x1301 + GROUP_HVAC + TYPE_FLOAT + AREA_SEAT
    const val HVAC_FAN_SPEED = 0x1302 + GROUP_HVAC + TYPE_INT32 + AREA_SEAT
    const val HVAC_FAN_DIRECTION = 0x1303 + GROUP_HVAC + TYPE_INT32 + AREA_SEAT
    const val HVAC_AC_ON = 0x1304 + GROUP_HVAC + TYPE_BOOLEAN + AREA_GLOBAL
    const val HVAC_SEAT_HEATING = 0x1305 + GROUP_HVAC + TYPE_INT32 + AREA_SEAT // 0: Off, 1: Low, 2: Med, 3: High
    const val HVAC_DEFROST_FRONT = 0x1306 + GROUP_HVAC + TYPE_BOOLEAN + AREA_GLOBAL
    const val HVAC_DEFROST_REAR = 0x1307 + GROUP_HVAC + TYPE_BOOLEAN + AREA_GLOBAL

    // Vehicle Properties
    const val VEHICLE_UNITS_DISTANCE = 0x1401 + GROUP_VEHICLE + TYPE_INT32 + AREA_GLOBAL // 0: km, 1: miles
    const val VEHICLE_UNITS_TEMP = 0x1402 + GROUP_VEHICLE + TYPE_INT32 + AREA_GLOBAL // 0: C, 1: F
    const val VEHICLE_DOOR_LOCK = 0x1403 + GROUP_VEHICLE + TYPE_BOOLEAN + AREA_DOOR
    const val VEHICLE_HEADLIGHTS_MODE = 0x1404 + GROUP_VEHICLE + TYPE_INT32 + AREA_GLOBAL // 0: Off, 1: Auto, 2: On
    const val VEHICLE_AMBIENT_LIGHT_COLOR = 0x1405 + GROUP_VEHICLE + TYPE_INT32 + AREA_GLOBAL // RGB
    const val VEHICLE_MIRROR_FOLD = 0x1406 + GROUP_VEHICLE + TYPE_BOOLEAN + AREA_MIRROR

    // Profile Properties
    const val PROFILE_NAME = 0x1501 + GROUP_PROFILE + TYPE_STRING + AREA_GLOBAL
    const val PROFILE_AVATAR_ID = 0x1502 + GROUP_PROFILE + TYPE_INT32 + AREA_GLOBAL
    const val PROFILE_SEAT_POS = 0x1503 + GROUP_PROFILE + TYPE_INT32 + AREA_SEAT

    // Privacy Properties
    const val PRIVACY_LOCATION_ENABLED = 0x1601 + GROUP_PRIVACY + TYPE_BOOLEAN + AREA_GLOBAL
    const val PRIVACY_ANALYTICS_ENABLED = 0x1602 + GROUP_PRIVACY + TYPE_BOOLEAN + AREA_GLOBAL
    const val PRIVACY_VOICE_DATA_ENABLED = 0x1603 + GROUP_PRIVACY + TYPE_BOOLEAN + AREA_GLOBAL

    fun getPropertyType(propertyId: Int): Int = propertyId and TYPE_MASK

    fun getAreaType(propertyId: Int): Int = propertyId and AREA_MASK

    fun isGlobalProperty(propertyId: Int): Boolean = getAreaType(propertyId) == AREA_GLOBAL

    fun getPropertyName(propertyId: Int): String {
        return when (propertyId) {
            DISPLAY_BRIGHTNESS -> "Display Brightness"
            DISPLAY_THEME -> "Display Theme"
            DISPLAY_LANGUAGE -> "Display Language"
            DISPLAY_NIGHT_MODE -> "Display Night Mode"
            AUDIO_VOLUME_MEDIA -> "Media Volume"
            AUDIO_VOLUME_NAV -> "Navigation Volume"
            HVAC_TEMPERATURE_SET -> "HVAC Temperature"
            HVAC_AC_ON -> "HVAC AC"
            VEHICLE_UNITS_DISTANCE -> "Distance Units"
            PRIVACY_LOCATION_ENABLED -> "Location Privacy"
            else -> "Unknown Property (0x${Integer.toHexString(propertyId)})"
        }
    }
}
