package com.example.carsettings.aidl;

import com.example.carsettings.aidl.PropertyValue;
import com.example.carsettings.aidl.CarSettingsError;

oneway interface ICarSettingsCallback {
    void onPropertyChanged(int propertyId, int areaId, in PropertyValue value);
    void onPropertyError(int propertyId, int areaId, in CarSettingsError error);
    void onServiceStatusChanged(int status);
    void onDriverProfileChanged(int oldProfileId, int newProfileId);
}
