package com.automotive.carsettings.aidl;

import com.automotive.carsettings.aidl.PropertyValue;
import com.automotive.carsettings.aidl.CarSettingsError;

/**
 * Callback interface for receiving property change notifications.
 * All methods are oneway to prevent blocking the service.
 */
interface ICarSettingsCallback {
    /**
     * Called when a property value changes.
     */
    oneway void onPropertyChanged(int propertyId, int areaId, in PropertyValue value);

    /**
     * Called when a property error occurs.
     */
    oneway void onPropertyError(int propertyId, int areaId, in CarSettingsError error);

    /**
     * Called when the service status changes.
     */
    oneway void onServiceStatusChanged(int status);

    /**
     * Called when the driver profile changes.
     */
    oneway void onDriverProfileChanged(int oldProfileId, int newProfileId);
}
