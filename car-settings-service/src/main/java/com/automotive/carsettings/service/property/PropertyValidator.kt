package com.automotive.carsettings.service.property

import com.automotive.carsettings.aidl.PropertyConstants
import com.automotive.carsettings.aidl.PropertyValue
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Validates property values against constraints.
 */
@Singleton
class PropertyValidator @Inject constructor() {

    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String = ""
    )

    /**
     * Validate a property value.
     */
    fun validate(value: PropertyValue): ValidationResult {
        return when (value.propertyId) {
            // Display properties
            PropertyConstants.DISPLAY_BRIGHTNESS -> {
                validateIntRange(value.intValue, 0, 255, "Brightness")
            }
            PropertyConstants.DISPLAY_THEME -> {
                validateIntRange(value.intValue, 0, 2, "Theme") // 0=light, 1=dark, 2=auto
            }
            PropertyConstants.DISPLAY_SCREEN_TIMEOUT -> {
                validateIntRange(value.intValue, 5000, 300000, "Screen timeout (ms)")
            }
            PropertyConstants.DISPLAY_FONT_SIZE -> {
                validateIntRange(value.intValue, 0, 4, "Font size")
            }

            // Audio properties
            PropertyConstants.AUDIO_VOLUME_MEDIA,
            PropertyConstants.AUDIO_VOLUME_NAVIGATION,
            PropertyConstants.AUDIO_VOLUME_CALL,
            PropertyConstants.AUDIO_VOLUME_VOICE_COMMAND,
            PropertyConstants.AUDIO_VOLUME_RINGTONE,
            PropertyConstants.AUDIO_VOLUME_ALARM -> {
                validateIntRange(value.intValue, 0, 30, "Audio volume")
            }
            PropertyConstants.AUDIO_BALANCE -> {
                validateIntRange(value.intValue, -10, 10, "Audio balance")
            }
            PropertyConstants.AUDIO_FADE -> {
                validateIntRange(value.intValue, -10, 10, "Audio fade")
            }
            PropertyConstants.AUDIO_BASS,
            PropertyConstants.AUDIO_TREBLE -> {
                validateIntRange(value.intValue, -10, 10, "Audio EQ")
            }
            PropertyConstants.AUDIO_EQ_PRESET -> {
                validateIntRange(value.intValue, 0, 5, "EQ preset")
            }

            // HVAC properties
            PropertyConstants.HVAC_TEMPERATURE -> {
                validateFloatRange(value.floatValue, 16.0f, 30.0f, "Temperature (°C)")
            }
            PropertyConstants.HVAC_FAN_SPEED -> {
                validateIntRange(value.intValue, 0, 7, "Fan speed")
            }
            PropertyConstants.HVAC_FAN_DIRECTION -> {
                validateIntRange(value.intValue, 0, 7, "Fan direction")
            }
            PropertyConstants.HVAC_SEAT_HEATING,
            PropertyConstants.HVAC_SEAT_VENTILATION -> {
                validateIntRange(value.intValue, 0, 3, "Seat heating/ventilation level")
            }
            PropertyConstants.HVAC_STEERING_WHEEL_HEAT -> {
                validateIntRange(value.intValue, 0, 2, "Steering wheel heat level")
            }

            // Vehicle properties
            PropertyConstants.VEHICLE_UNITS_DISTANCE,
            PropertyConstants.VEHICLE_UNITS_TEMPERATURE,
            PropertyConstants.VEHICLE_UNITS_VOLUME,
            PropertyConstants.VEHICLE_UNITS_PRESSURE -> {
                validateIntRange(value.intValue, 0, 2, "Unit selection")
            }
            PropertyConstants.VEHICLE_AMBIENT_LIGHT_BRIGHTNESS -> {
                validateIntRange(value.intValue, 0, 100, "Ambient light brightness")
            }

            // String properties
            PropertyConstants.DISPLAY_LANGUAGE,
            PropertyConstants.PROFILE_NAME,
            PropertyConstants.PROFILE_AVATAR -> {
                validateStringLength(value.stringValue, 0, 255, "String value")
            }

            // Boolean and other properties - always valid
            else -> ValidationResult(true)
        }
    }

    private fun validateIntRange(value: Int, min: Int, max: Int, name: String): ValidationResult {
        return if (value in min..max) {
            ValidationResult(true)
        } else {
            ValidationResult(false, "$name must be between $min and $max, got $value")
        }
    }

    private fun validateFloatRange(value: Float, min: Float, max: Float, name: String): ValidationResult {
        return if (value >= min && value <= max) {
            ValidationResult(true)
        } else {
            ValidationResult(false, "$name must be between $min and $max, got $value")
        }
    }

    private fun validateStringLength(value: String, min: Int, max: Int, name: String): ValidationResult {
        return if (value.length in min..max) {
            ValidationResult(true)
        } else {
            ValidationResult(false, "$name length must be between $min and $max, got ${value.length}")
        }
    }
}
