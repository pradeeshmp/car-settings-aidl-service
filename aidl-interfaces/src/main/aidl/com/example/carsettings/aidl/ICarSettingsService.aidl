package com.example.carsettings.aidl;

import com.example.carsettings.aidl.PropertyValue;
import com.example.carsettings.aidl.CarPropertyInfo;
import com.example.carsettings.aidl.CarPropertyConfig;
import com.example.carsettings.aidl.ICarSettingsCallback;
import com.example.carsettings.aidl.IPropertyEventListener;

interface ICarSettingsService {
    // Property CRUD
    PropertyValue getProperty(int propertyId, int areaId);
    void setProperty(in PropertyValue value);
    PropertyValue getPropertyWithArea(int propertyId, int areaId);
    void setPropertyWithArea(int propertyId, int areaId, in PropertyValue value);

    // Batch operations
    PropertyValue[] getPropertiesBatch(in int[] propertyIds, in int[] areaIds);
    void setPropertiesBatch(in PropertyValue[] values);

    // Metadata
    int[] getPropertyIds();
    CarPropertyInfo getPropertyInfo(int propertyId);
    CarPropertyConfig getPropertyConfig(int propertyId);
    boolean isPropertyAvailable(int propertyId, int areaId);
    int[] getSupportedAreas(int propertyId);

    // Callbacks
    void registerCallback(ICarSettingsCallback callback);
    void unregisterCallback(ICarSettingsCallback callback);
    void registerPropertyListener(int propertyId, IPropertyEventListener listener);
    void unregisterPropertyListener(int propertyId, IPropertyEventListener listener);

    // Permissions
    boolean checkPropertyPermission(int propertyId, int permissionLevel);
    int getRequiredPermissionLevel(int propertyId);

    // Driver profiles
    int getCurrentDriverProfileId();
    void switchDriverProfile(int profileId);
    int[] getDriverProfileIds();

    // Diagnostics
    int getConnectedClientCount();
    String dumpServiceState();
}
