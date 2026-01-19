package com.automotive.carsettings.client.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.automotive.carsettings.aidl.AreaConstants
import com.automotive.carsettings.aidl.PropertyConstants
import com.automotive.carsettings.client.manager.CarSettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for the Dashboard screen.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val carSettingsManager: CarSettingsManager
) : ViewModel() {

    data class DashboardState(
        val connectionStatus: String = "Disconnected",
        val serviceStatus: String = "Unknown",
        val propertyCount: Int = 0,
        val brightness: Int = 0,
        val mediaVolume: Int = 0,
        val driverTemperature: Float = 0f
    )

    private val _dashboardState = MutableStateFlow(DashboardState())
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()

    init {
        observeConnectionState()
        observeServiceStatus()
        loadInitialData()
    }

    private fun observeConnectionState() {
        viewModelScope.launch {
            carSettingsManager.connectionState.collect { state ->
                _dashboardState.value = _dashboardState.value.copy(
                    connectionStatus = when (state) {
                        is CarSettingsManager.ConnectionState.Connected -> "Connected"
                        is CarSettingsManager.ConnectionState.Connecting -> "Connecting..."
                        is CarSettingsManager.ConnectionState.Disconnected -> "Disconnected"
                        is CarSettingsManager.ConnectionState.Error -> "Error: ${state.message}"
                    }
                )
            }
        }
    }

    private fun observeServiceStatus() {
        viewModelScope.launch {
            carSettingsManager.serviceStatus.collect { status ->
                _dashboardState.value = _dashboardState.value.copy(
                    serviceStatus = when (status) {
                        0 -> "Initializing"
                        1 -> "Ready"
                        2 -> "Shutting Down"
                        3 -> "Error"
                        4 -> "Suspended"
                        else -> "Unknown"
                    }
                )
            }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            // Get property count
            carSettingsManager.getAllPropertyIds().onSuccess { ids ->
                _dashboardState.value = _dashboardState.value.copy(propertyCount = ids.size)
            }

            // Get brightness
            carSettingsManager.getIntProperty(PropertyConstants.DISPLAY_BRIGHTNESS).onSuccess { brightness ->
                _dashboardState.value = _dashboardState.value.copy(brightness = brightness)
            }

            // Get media volume
            carSettingsManager.getIntProperty(PropertyConstants.AUDIO_VOLUME_MEDIA).onSuccess { volume ->
                _dashboardState.value = _dashboardState.value.copy(mediaVolume = volume)
            }

            // Get driver temperature
            carSettingsManager.getFloatProperty(
                PropertyConstants.HVAC_TEMPERATURE,
                AreaConstants.SEAT_DRIVER
            ).onSuccess { temp ->
                _dashboardState.value = _dashboardState.value.copy(driverTemperature = temp)
            }
        }
    }

    fun refresh() {
        loadInitialData()
    }

    fun dumpServiceState() {
        viewModelScope.launch {
            carSettingsManager.dumpServiceState().onSuccess { dump ->
                Timber.d("Service state dump:\n$dump")
            }
        }
    }
}
