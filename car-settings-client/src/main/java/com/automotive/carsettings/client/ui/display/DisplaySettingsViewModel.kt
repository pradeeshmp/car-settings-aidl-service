package com.automotive.carsettings.client.ui.display

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
 * ViewModel for Display Settings.
 */
@HiltViewModel
class DisplaySettingsViewModel @Inject constructor(
    private val carSettingsManager: CarSettingsManager
) : ViewModel() {

    data class DisplayState(
        val brightness: Int = 128,
        val autoBrightness: Boolean = true,
        val nightMode: Boolean = false,
        val theme: Int = 0 // 0=light, 1=dark, 2=auto
    )

    private val _displayState = MutableStateFlow(DisplayState())
    val displayState: StateFlow<DisplayState> = _displayState.asStateFlow()

    init {
        loadDisplaySettings()
        observePropertyChanges()
    }

    private fun loadDisplaySettings() {
        viewModelScope.launch {
            // Load brightness
            carSettingsManager.getIntProperty(PropertyConstants.DISPLAY_BRIGHTNESS).onSuccess { value ->
                _displayState.value = _displayState.value.copy(brightness = value)
            }

            // Load auto brightness
            carSettingsManager.getBooleanProperty(PropertyConstants.DISPLAY_AUTO_BRIGHTNESS).onSuccess { value ->
                _displayState.value = _displayState.value.copy(autoBrightness = value)
            }

            // Load night mode
            carSettingsManager.getBooleanProperty(PropertyConstants.DISPLAY_NIGHT_MODE).onSuccess { value ->
                _displayState.value = _displayState.value.copy(nightMode = value)
            }

            // Load theme
            carSettingsManager.getIntProperty(PropertyConstants.DISPLAY_THEME).onSuccess { value ->
                _displayState.value = _displayState.value.copy(theme = value)
            }
        }
    }

    private fun observePropertyChanges() {
        viewModelScope.launch {
            carSettingsManager.propertyChanges.collect { event ->
                when (event.propertyId) {
                    PropertyConstants.DISPLAY_BRIGHTNESS -> {
                        _displayState.value = _displayState.value.copy(brightness = event.value.intValue)
                    }
                    PropertyConstants.DISPLAY_AUTO_BRIGHTNESS -> {
                        _displayState.value = _displayState.value.copy(autoBrightness = event.value.booleanValue)
                    }
                    PropertyConstants.DISPLAY_NIGHT_MODE -> {
                        _displayState.value = _displayState.value.copy(nightMode = event.value.booleanValue)
                    }
                    PropertyConstants.DISPLAY_THEME -> {
                        _displayState.value = _displayState.value.copy(theme = event.value.intValue)
                    }
                }
            }
        }
    }

    fun setBrightness(value: Int) {
        viewModelScope.launch {
            carSettingsManager.setIntProperty(PropertyConstants.DISPLAY_BRIGHTNESS, value = value)
                .onFailure { Timber.e(it, "Failed to set brightness") }
        }
    }

    fun setAutoBrightness(enabled: Boolean) {
        viewModelScope.launch {
            carSettingsManager.setBooleanProperty(PropertyConstants.DISPLAY_AUTO_BRIGHTNESS, value = enabled)
                .onFailure { Timber.e(it, "Failed to set auto brightness") }
        }
    }

    fun setNightMode(enabled: Boolean) {
        viewModelScope.launch {
            carSettingsManager.setBooleanProperty(PropertyConstants.DISPLAY_NIGHT_MODE, value = enabled)
                .onFailure { Timber.e(it, "Failed to set night mode") }
        }
    }

    fun setTheme(theme: Int) {
        viewModelScope.launch {
            carSettingsManager.setIntProperty(PropertyConstants.DISPLAY_THEME, value = theme)
                .onFailure { Timber.e(it, "Failed to set theme") }
        }
    }
}
