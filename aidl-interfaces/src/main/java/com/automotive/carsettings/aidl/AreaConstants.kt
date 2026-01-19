package com.automotive.carsettings.aidl

/**
 * Vehicle area ID constants following Android Automotive VehicleArea pattern.
 */
object AreaConstants {

    // Global area (used for vehicle-wide properties)
    const val AREA_GLOBAL = 0

    // Seat areas (bit flags)
    const val SEAT_ROW_1_LEFT = 0x0001       // Driver seat
    const val SEAT_ROW_1_CENTER = 0x0002
    const val SEAT_ROW_1_RIGHT = 0x0004      // Front passenger
    const val SEAT_ROW_2_LEFT = 0x0010
    const val SEAT_ROW_2_CENTER = 0x0020
    const val SEAT_ROW_2_RIGHT = 0x0040
    const val SEAT_ROW_3_LEFT = 0x0100
    const val SEAT_ROW_3_CENTER = 0x0200
    const val SEAT_ROW_3_RIGHT = 0x0400

    // Convenience aliases
    const val SEAT_DRIVER = SEAT_ROW_1_LEFT
    const val SEAT_PASSENGER = SEAT_ROW_1_RIGHT
    const val SEAT_ALL = 0x0777

    // Door areas (bit flags)
    const val DOOR_ROW_1_LEFT = 0x00000001
    const val DOOR_ROW_1_RIGHT = 0x00000004
    const val DOOR_ROW_2_LEFT = 0x00000010
    const val DOOR_ROW_2_RIGHT = 0x00000040
    const val DOOR_HOOD = 0x10000000
    const val DOOR_REAR = 0x20000000
    const val DOOR_ALL = 0x30000055

    // Window areas (bit flags)
    const val WINDOW_ROW_1_LEFT = 0x00000001
    const val WINDOW_ROW_1_RIGHT = 0x00000004
    const val WINDOW_ROW_2_LEFT = 0x00000010
    const val WINDOW_ROW_2_RIGHT = 0x00000040
    const val WINDOW_FRONT_WINDSHIELD = 0x00000001 shl 8
    const val WINDOW_REAR_WINDSHIELD = 0x00000002 shl 8
    const val WINDOW_ROOF_TOP = 0x00000004 shl 8

    // Mirror areas (bit flags)
    const val MIRROR_DRIVER_LEFT = 0x00000001
    const val MIRROR_DRIVER_RIGHT = 0x00000002
    const val MIRROR_DRIVER_CENTER = 0x00000004
    const val MIRROR_ALL = 0x00000007

    // Wheel areas (bit flags)
    const val WHEEL_LEFT_FRONT = 0x00000001
    const val WHEEL_RIGHT_FRONT = 0x00000002
    const val WHEEL_LEFT_REAR = 0x00000004
    const val WHEEL_RIGHT_REAR = 0x00000008
    const val WHEEL_ALL = 0x0000000F

    // HVAC Fan direction flags
    const val FAN_DIRECTION_FACE = 0x01
    const val FAN_DIRECTION_FLOOR = 0x02
    const val FAN_DIRECTION_DEFROST = 0x04
    const val FAN_DIRECTION_FACE_AND_FLOOR = FAN_DIRECTION_FACE or FAN_DIRECTION_FLOOR

    /**
     * Get a human-readable name for a seat area ID.
     */
    fun getSeatAreaName(areaId: Int): String {
        return when (areaId) {
            AREA_GLOBAL -> "Global"
            SEAT_ROW_1_LEFT -> "Driver Seat"
            SEAT_ROW_1_CENTER -> "Front Center Seat"
            SEAT_ROW_1_RIGHT -> "Front Passenger Seat"
            SEAT_ROW_2_LEFT -> "Rear Left Seat"
            SEAT_ROW_2_CENTER -> "Rear Center Seat"
            SEAT_ROW_2_RIGHT -> "Rear Right Seat"
            SEAT_ROW_3_LEFT -> "Third Row Left Seat"
            SEAT_ROW_3_CENTER -> "Third Row Center Seat"
            SEAT_ROW_3_RIGHT -> "Third Row Right Seat"
            else -> "Area_$areaId"
        }
    }

    /**
     * Get a human-readable name for a door area ID.
     */
    fun getDoorAreaName(areaId: Int): String {
        return when (areaId) {
            DOOR_ROW_1_LEFT -> "Driver Door"
            DOOR_ROW_1_RIGHT -> "Front Passenger Door"
            DOOR_ROW_2_LEFT -> "Rear Left Door"
            DOOR_ROW_2_RIGHT -> "Rear Right Door"
            DOOR_HOOD -> "Hood"
            DOOR_REAR -> "Trunk/Rear Door"
            else -> "Door_$areaId"
        }
    }

    /**
     * Get a human-readable name for a window area ID.
     */
    fun getWindowAreaName(areaId: Int): String {
        return when (areaId) {
            WINDOW_ROW_1_LEFT -> "Driver Window"
            WINDOW_ROW_1_RIGHT -> "Front Passenger Window"
            WINDOW_ROW_2_LEFT -> "Rear Left Window"
            WINDOW_ROW_2_RIGHT -> "Rear Right Window"
            WINDOW_FRONT_WINDSHIELD -> "Front Windshield"
            WINDOW_REAR_WINDSHIELD -> "Rear Windshield"
            WINDOW_ROOF_TOP -> "Sunroof"
            else -> "Window_$areaId"
        }
    }

    /**
     * Get a human-readable name for a mirror area ID.
     */
    fun getMirrorAreaName(areaId: Int): String {
        return when (areaId) {
            MIRROR_DRIVER_LEFT -> "Left Mirror"
            MIRROR_DRIVER_RIGHT -> "Right Mirror"
            MIRROR_DRIVER_CENTER -> "Center Mirror"
            else -> "Mirror_$areaId"
        }
    }
}
