package com.example.carsettings.constants

object AreaConstants {
    // Global area
    const val AREA_GLOBAL = 0x0

    // Seat areas
    const val SEAT_DRIVER = 0x01
    const val SEAT_PASSENGER = 0x02
    const val SEAT_ROW_1_CENTER = 0x04
    const val SEAT_ROW_2_LEFT = 0x10
    const val SEAT_ROW_2_CENTER = 0x20
    const val SEAT_ROW_2_RIGHT = 0x40

    // Door areas
    const val DOOR_ROW_1_LEFT = 0x01
    const val DOOR_ROW_1_RIGHT = 0x02
    const val DOOR_ROW_2_LEFT = 0x04
    const val DOOR_ROW_2_RIGHT = 0x08

    // Mirror areas
    const val MIRROR_DRIVER = 0x01
    const val MIRROR_PASSENGER = 0x02
    const val MIRROR_REARVIEW = 0x04

    // Window areas
    const val WINDOW_ROW_1_LEFT = 0x01
    const val WINDOW_ROW_1_RIGHT = 0x02
    const val WINDOW_ROW_2_LEFT = 0x04
    const val WINDOW_ROW_2_RIGHT = 0x08
    const val WINDOW_ROOF_TOP = 0x10

    // HVAC Fan Directions
    const val FAN_DIRECTION_FACE = 0x01
    const val FAN_DIRECTION_FLOOR = 0x02
    const val FAN_DIRECTION_DEFROST = 0x04

    fun getSeatAreaName(areaId: Int): String {
        return when (areaId) {
            SEAT_DRIVER -> "Driver Seat"
            SEAT_PASSENGER -> "Passenger Seat"
            else -> "Seat ($areaId)"
        }
    }
}
