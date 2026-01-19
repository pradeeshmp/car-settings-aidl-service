package com.automotive.carsettings.service.property

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.automotive.carsettings.aidl.AreaConstants
import com.automotive.carsettings.aidl.PropertyConstants
import com.automotive.carsettings.aidl.PropertyValue
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

private val Context.profileDataStore by preferencesDataStore("car_settings_profile")

/**
 * Thread-safe storage for property values with persistence support.
 * Properties are keyed by "propertyId:areaId" combination.
 */
@Singleton
class PropertyStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val validator: PropertyValidator
) {

    private val properties = ConcurrentHashMap<String, PropertyValue>()
    private var currentProfileId = 1

    init {
        initializeDefaults()
    }

    /**
     * Get a property value.
     */
    fun getProperty(propertyId: Int, areaId: Int): PropertyValue? {
        val key = makeKey(propertyId, areaId)
        return properties[key]
    }

    /**
     * Set a property value with validation.
     */
    fun setProperty(value: PropertyValue): Boolean {
        // Validate value
        val validationResult = validator.validate(value)
        if (!validationResult.isValid) {
            Timber.w("Validation failed for property ${value.propertyId}: ${validationResult.errorMessage}")
            return false
        }

        val key = makeKey(value.propertyId, value.areaId)
        properties[key] = value.copy(timestampNanos = System.nanoTime())

        Timber.d("Property set: $key = $value")
        return true
    }

    /**
     * Load a driver profile from persistent storage.
     */
    fun loadProfile(profileId: Int) {
        Timber.d("Loading profile $profileId")
        currentProfileId = profileId

        runBlocking {
            context.profileDataStore.data.first().asMap().forEach { (key, value) ->
                if (key.name.startsWith("profile_${profileId}_")) {
                    val parts = key.name.removePrefix("profile_${profileId}_").split("_")
                    if (parts.size == 2) {
                        val propertyId = parts[0].toIntOrNull() ?: return@forEach
                        val areaId = parts[1].toIntOrNull() ?: return@forEach

                        // Restore property value
                        val propertyValue = when (value) {
                            is Int -> PropertyValue.forInt(propertyId, areaId, value)
                            is Float -> PropertyValue.forFloat(propertyId, areaId, value)
                            is Boolean -> PropertyValue.forBoolean(propertyId, areaId, value)
                            is String -> PropertyValue.forString(propertyId, areaId, value)
                            else -> null
                        }

                        propertyValue?.let { properties[makeKey(propertyId, areaId)] = it }
                    }
                }
            }
        }

        Timber.d("Loaded ${properties.size} properties for profile $profileId")
    }

    /**
     * Save current properties to persistent storage for a driver profile.
     */
    fun saveProfile(profileId: Int) {
        Timber.d("Saving profile $profileId")

        runBlocking {
            context.profileDataStore.edit { prefs ->
                properties.forEach { (key, value) ->
                    val dataStoreKey = "profile_${profileId}_${value.propertyId}_${value.areaId}"

                    when (PropertyConstants.getPropertyType(value.propertyId)) {
                        PropertyConstants.TYPE_INT32 -> {
                            prefs[intPreferencesKey(dataStoreKey)] = value.intValue
                        }
                        PropertyConstants.TYPE_FLOAT -> {
                            prefs[floatPreferencesKey(dataStoreKey)] = value.floatValue
                        }
                        PropertyConstants.TYPE_BOOLEAN -> {
                            prefs[booleanPreferencesKey(dataStoreKey)] = value.booleanValue
                        }
                        PropertyConstants.TYPE_STRING -> {
                            prefs[stringPreferencesKey(dataStoreKey)] = value.stringValue
                        }
                    }
                }
            }
        }

        Timber.d("Saved ${properties.size} properties for profile $profileId")
    }

    /**
     * Get a dump of all properties for diagnostics.
     */
    fun dump(): String {
        val sb = StringBuilder()
        properties.entries.sortedBy { it.key }.forEach { (key, value) ->
            sb.appendLine("  $key -> ${formatPropertyValue(value)}")
        }
        return sb.toString()
    }

    private fun makeKey(propertyId: Int, areaId: Int): String {
        return "$propertyId:$areaId"
    }

    private fun formatPropertyValue(value: PropertyValue): String {
        return when (PropertyConstants.getPropertyType(value.propertyId)) {
            PropertyConstants.TYPE_INT32 -> value.intValue.toString()
            PropertyConstants.TYPE_FLOAT -> value.floatValue.toString()
            PropertyConstants.TYPE_BOOLEAN -> value.booleanValue.toString()
            PropertyConstants.TYPE_STRING -> value.stringValue
            else -> "Unknown"
        }
    }

    /**
     * Initialize default values for all properties.
     */
    private fun initializeDefaults() {
        Timber.d("Initializing default property values")

        // Display defaults
        setProperty(PropertyValue.forInt(PropertyConstants.DISPLAY_BRIGHTNESS, AreaConstants.AREA_GLOBAL, 128))
        setProperty(PropertyValue.forInt(PropertyConstants.DISPLAY_THEME, AreaConstants.AREA_GLOBAL, 0)) // Light theme
        setProperty(PropertyValue.forString(PropertyConstants.DISPLAY_LANGUAGE, AreaConstants.AREA_GLOBAL, "en_US"))
        setProperty(PropertyValue.forBoolean(PropertyConstants.DISPLAY_NIGHT_MODE, AreaConstants.AREA_GLOBAL, false))
        setProperty(PropertyValue.forBoolean(PropertyConstants.DISPLAY_AUTO_BRIGHTNESS, AreaConstants.AREA_GLOBAL, true))

        // Audio defaults
        setProperty(PropertyValue.forInt(PropertyConstants.AUDIO_VOLUME_MEDIA, AreaConstants.AREA_GLOBAL, 15))
        setProperty(PropertyValue.forInt(PropertyConstants.AUDIO_VOLUME_NAVIGATION, AreaConstants.AREA_GLOBAL, 20))
        setProperty(PropertyValue.forInt(PropertyConstants.AUDIO_VOLUME_CALL, AreaConstants.AREA_GLOBAL, 25))
        setProperty(PropertyValue.forInt(PropertyConstants.AUDIO_BALANCE, AreaConstants.AREA_GLOBAL, 0))
        setProperty(PropertyValue.forInt(PropertyConstants.AUDIO_FADE, AreaConstants.AREA_GLOBAL, 0))
        setProperty(PropertyValue.forInt(PropertyConstants.AUDIO_EQ_PRESET, AreaConstants.AREA_GLOBAL, 0))

        // HVAC defaults
        setProperty(PropertyValue.forFloat(PropertyConstants.HVAC_TEMPERATURE, AreaConstants.SEAT_DRIVER, 22.0f))
        setProperty(PropertyValue.forFloat(PropertyConstants.HVAC_TEMPERATURE, AreaConstants.SEAT_PASSENGER, 22.0f))
        setProperty(PropertyValue.forInt(PropertyConstants.HVAC_FAN_SPEED, AreaConstants.SEAT_DRIVER, 3))
        setProperty(PropertyValue.forInt(PropertyConstants.HVAC_FAN_SPEED, AreaConstants.SEAT_PASSENGER, 3))
        setProperty(PropertyValue.forBoolean(PropertyConstants.HVAC_AC_ON, AreaConstants.AREA_GLOBAL, false))
        setProperty(PropertyValue.forBoolean(PropertyConstants.HVAC_AUTO_MODE, AreaConstants.AREA_GLOBAL, true))

        // Vehicle defaults
        setProperty(PropertyValue.forBoolean(PropertyConstants.VEHICLE_HEADLIGHTS_AUTO, AreaConstants.AREA_GLOBAL, true))
        setProperty(PropertyValue.forInt(PropertyConstants.VEHICLE_AMBIENT_LIGHT_BRIGHTNESS, AreaConstants.AREA_GLOBAL, 50))

        // Privacy defaults
        setProperty(PropertyValue.forBoolean(PropertyConstants.PRIVACY_LOCATION_ENABLED, AreaConstants.AREA_GLOBAL, true))
        setProperty(PropertyValue.forBoolean(PropertyConstants.PRIVACY_ANALYTICS_ENABLED, AreaConstants.AREA_GLOBAL, false))
        setProperty(PropertyValue.forBoolean(PropertyConstants.PRIVACY_PERSONALIZATION_ENABLED, AreaConstants.AREA_GLOBAL, true))

        Timber.d("Initialized ${properties.size} default properties")
    }
}
