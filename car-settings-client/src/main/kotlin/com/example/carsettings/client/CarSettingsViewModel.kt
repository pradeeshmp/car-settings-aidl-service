package com.example.carsettings.client

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carsettings.aidl.*
import com.example.carsettings.constants.PropertyConstants
import com.example.carsettings.constants.AreaConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class CarSettingsUiState(
    val isConnected: Boolean = false,
    val brightness: Int = 0,
    val hvacTempDriver: Float = 22.0f,
    val hvacTempPassenger: Float = 22.0f,
    val isNightMode: Boolean = false,
    val currentProfileId: Int = 1
)

@HiltViewModel
class CarSettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(CarSettingsUiState())
    val uiState: StateFlow<CarSettingsUiState> = _uiState

    private var carSettingsService: ICarSettingsService? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Timber.i("Connected to CarSettingsService")
            carSettingsService = ICarSettingsService.Stub.asInterface(service)
            _uiState.value = _uiState.value.copy(isConnected = true)
            
            try {
                carSettingsService?.registerCallback(callback)
                fetchInitialData()
            } catch (e: Exception) {
                Timber.e(e, "Error registering callback or fetching data")
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Timber.i("Disconnected from CarSettingsService")
            carSettingsService = null
            _uiState.value = _uiState.value.copy(isConnected = false)
        }
    }

    private val callback = object : ICarSettingsCallback.Stub() {
        override fun onPropertyChanged(propertyId: Int, areaId: Int, value: PropertyValue) {
            Timber.d("onPropertyChanged: propertyId=0x${Integer.toHexString(propertyId)}, areaId=$areaId, value=$value")
            updateStateFromValue(value)
        }

        override fun onPropertyError(propertyId: Int, areaId: Int, error: CarSettingsError) {
            Timber.e("onPropertyError: propertyId=0x${Integer.toHexString(propertyId)}, error=$error")
        }

        override fun onServiceStatusChanged(status: Int) {
            Timber.i("onServiceStatusChanged: status=$status")
        }

        override fun onDriverProfileChanged(oldProfileId: Int, newProfileId: Int) {
            Timber.i("onDriverProfileChanged: $oldProfileId -> $newProfileId")
            _uiState.value = _uiState.value.copy(currentProfileId = newProfileId)
        }
    }

    init {
        bindService()
    }

    private fun bindService() {
        val intent = Intent("com.example.carsettings.SERVICE_BIND")
        intent.setPackage("com.example.carsettings.service")
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun fetchInitialData() {
        viewModelScope.launch {
            try {
                val service = carSettingsService ?: return@launch
                
                val brightness = service.getProperty(PropertyConstants.DISPLAY_BRIGHTNESS, AreaConstants.AREA_GLOBAL).intValue
                val tempDriver = service.getProperty(PropertyConstants.HVAC_TEMPERATURE_SET, AreaConstants.SEAT_DRIVER).floatValue
                val tempPassenger = service.getProperty(PropertyConstants.HVAC_TEMPERATURE_SET, AreaConstants.SEAT_PASSENGER).floatValue
                val nightMode = service.getProperty(PropertyConstants.DISPLAY_NIGHT_MODE, AreaConstants.AREA_GLOBAL).booleanValue
                val profileId = service.getCurrentDriverProfileId()

                _uiState.value = _uiState.value.copy(
                    brightness = brightness,
                    hvacTempDriver = tempDriver,
                    hvacTempPassenger = tempPassenger,
                    isNightMode = nightMode,
                    currentProfileId = profileId
                )
            } catch (e: Exception) {
                Timber.e(e, "Error fetching initial data")
            }
        }
    }

    fun setBrightness(value: Int) {
        viewModelScope.launch {
            try {
                val propValue = PropertyValue().apply {
                    propertyId = PropertyConstants.DISPLAY_BRIGHTNESS
                    areaId = AreaConstants.AREA_GLOBAL
                    intValue = value
                }
                carSettingsService?.setProperty(propValue)
            } catch (e: Exception) {
                Timber.e(e, "Error setting brightness")
            }
        }
    }

    fun setHvacTemp(areaId: Int, value: Float) {
        viewModelScope.launch {
            try {
                val propValue = PropertyValue().apply {
                    propertyId = PropertyConstants.HVAC_TEMPERATURE_SET
                    this.areaId = areaId
                    floatValue = value
                }
                carSettingsService?.setProperty(propValue)
            } catch (e: Exception) {
                Timber.e(e, "Error setting HVAC temp")
            }
        }
    }

    fun setNightMode(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val propValue = PropertyValue().apply {
                    propertyId = PropertyConstants.DISPLAY_NIGHT_MODE
                    areaId = AreaConstants.AREA_GLOBAL
                    booleanValue = enabled
                }
                carSettingsService?.setProperty(propValue)
            } catch (e: Exception) {
                Timber.e(e, "Error setting night mode")
            }
        }
    }

    private fun updateStateFromValue(value: PropertyValue) {
        when (value.propertyId) {
            PropertyConstants.DISPLAY_BRIGHTNESS -> {
                _uiState.value = _uiState.value.copy(brightness = value.intValue)
            }
            PropertyConstants.HVAC_TEMPERATURE_SET -> {
                if (value.areaId == AreaConstants.SEAT_DRIVER) {
                    _uiState.value = _uiState.value.copy(hvacTempDriver = value.floatValue)
                } else if (value.areaId == AreaConstants.SEAT_PASSENGER) {
                    _uiState.value = _uiState.value.copy(hvacTempPassenger = value.floatValue)
                }
            }
            PropertyConstants.DISPLAY_NIGHT_MODE -> {
                _uiState.value = _uiState.value.copy(isNightMode = value.booleanValue)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            carSettingsService?.unregisterCallback(callback)
        } catch (e: Exception) {
            Timber.e(e, "Error unregistering callback")
        }
        context.unbindService(serviceConnection)
    }
}
