package com.automotive.carsettings.aidl;

import com.automotive.carsettings.aidl.PropertyValue;

/**
 * Lightweight listener for single property events.
 * Useful for observing specific properties without the overhead of ICarSettingsCallback.
 */
interface IPropertyEventListener {
    /**
     * Called when the observed property value changes.
     */
    oneway void onPropertyEvent(int propertyId, int areaId, in PropertyValue value);
}
