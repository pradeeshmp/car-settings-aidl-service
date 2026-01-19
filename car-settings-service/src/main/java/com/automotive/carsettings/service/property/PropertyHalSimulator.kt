package com.automotive.carsettings.service.property

import com.automotive.carsettings.aidl.AreaConstants
import com.automotive.carsettings.aidl.PropertyConstants
import com.automotive.carsettings.aidl.PropertyValue
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Simulates HAL (Hardware Abstraction Layer) behavior by generating periodic property updates.
 * In a real AAOS system, this would interface with the Vehicle HAL.
 */
@Singleton
class PropertyHalSimulator @Inject constructor() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var simulationJob: Job? = null
    private var callback: ((Int, Int, PropertyValue) -> Unit)? = null

    /**
     * Start the HAL simulator with a callback for property changes.
     */
    fun start(onPropertyChanged: (propertyId: Int, areaId: Int, value: PropertyValue) -> Unit) {
        Timber.d("Starting HAL simulator")
        callback = onPropertyChanged

        simulationJob = scope.launch {
            while (isActive) {
                delay(5000) // Update every 5 seconds

                // Simulate temperature sensor fluctuation
                simulateTemperatureChange()

                // Occasionally simulate other property changes
                if (Random.nextFloat() < 0.2f) {
                    simulateRandomPropertyChange()
                }
            }
        }
    }

    /**
     * Stop the HAL simulator.
     */
    fun stop() {
        Timber.d("Stopping HAL simulator")
        simulationJob?.cancel()
        callback = null
    }

    private fun simulateTemperatureChange() {
        // Simulate slight temperature changes in driver and passenger zones
        val driverTemp = 22.0f + Random.nextFloat() * 2.0f - 1.0f  // 21-23°C
        val passengerTemp = 22.0f + Random.nextFloat() * 2.0f - 1.0f

        callback?.invoke(
            PropertyConstants.HVAC_TEMPERATURE,
            AreaConstants.SEAT_DRIVER,
            PropertyValue.forFloat(PropertyConstants.HVAC_TEMPERATURE, AreaConstants.SEAT_DRIVER, driverTemp)
        )

        callback?.invoke(
            PropertyConstants.HVAC_TEMPERATURE,
            AreaConstants.SEAT_PASSENGER,
            PropertyValue.forFloat(PropertyConstants.HVAC_TEMPERATURE, AreaConstants.SEAT_PASSENGER, passengerTemp)
        )
    }

    private fun simulateRandomPropertyChange() {
        // Randomly change one of several properties to simulate real-world changes
        when (Random.nextInt(5)) {
            0 -> {
                // Simulate auto brightness adjustment
                val brightness = 128 + Random.nextInt(64) - 32 // 96-160
                callback?.invoke(
                    PropertyConstants.DISPLAY_BRIGHTNESS,
                    AreaConstants.AREA_GLOBAL,
                    PropertyValue.forInt(PropertyConstants.DISPLAY_BRIGHTNESS, AreaConstants.AREA_GLOBAL, brightness)
                )
            }
            1 -> {
                // Simulate night mode toggle based on time
                val isNightMode = Random.nextBoolean()
                callback?.invoke(
                    PropertyConstants.DISPLAY_NIGHT_MODE,
                    AreaConstants.AREA_GLOBAL,
                    PropertyValue.forBoolean(PropertyConstants.DISPLAY_NIGHT_MODE, AreaConstants.AREA_GLOBAL, isNightMode)
                )
            }
            2 -> {
                // Simulate fan speed adjustment
                val fanSpeed = Random.nextInt(8)
                callback?.invoke(
                    PropertyConstants.HVAC_FAN_SPEED,
                    AreaConstants.SEAT_DRIVER,
                    PropertyValue.forInt(PropertyConstants.HVAC_FAN_SPEED, AreaConstants.SEAT_DRIVER, fanSpeed)
                )
            }
            3 -> {
                // Simulate ambient light brightness change
                val ambientBrightness = Random.nextInt(101)
                callback?.invoke(
                    PropertyConstants.VEHICLE_AMBIENT_LIGHT_BRIGHTNESS,
                    AreaConstants.AREA_GLOBAL,
                    PropertyValue.forInt(PropertyConstants.VEHICLE_AMBIENT_LIGHT_BRIGHTNESS, AreaConstants.AREA_GLOBAL, ambientBrightness)
                )
            }
            4 -> {
                // Simulate AC state change
                val acOn = Random.nextBoolean()
                callback?.invoke(
                    PropertyConstants.HVAC_AC_ON,
                    AreaConstants.AREA_GLOBAL,
                    PropertyValue.forBoolean(PropertyConstants.HVAC_AC_ON, AreaConstants.AREA_GLOBAL, acOn)
                )
            }
        }

        Timber.d("Simulated random property change")
    }
}
