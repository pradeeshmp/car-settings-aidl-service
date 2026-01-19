package com.automotive.carsettings.aidl;

import com.automotive.carsettings.aidl.ICarSettingsCallback;
import com.automotive.carsettings.aidl.IPropertyEventListener;
import com.automotive.carsettings.aidl.PropertyValue;
import com.automotive.carsettings.aidl.CarPropertyInfo;
import com.automotive.carsettings.aidl.CarPropertyConfig;
import com.automotive.carsettings.aidl.CarSettingsError;

/**
 * Main AIDL interface for the Car Settings Service.
 * Provides vehicle property management similar to Android Automotive's CarPropertyService.
 */
interface ICarSettingsService {
    /**
     * Property CRUD Operations
     */
    PropertyValue getProperty(int propertyId, int areaId);
    boolean setProperty(in PropertyValue value);
    PropertyValue getPropertyWithArea(int propertyId, int areaId);
    boolean setPropertyWithArea(int propertyId, int areaId, in PropertyValue value);

    /**
     * Batch Operations
     */
    List<PropertyValue> getPropertiesBatch(in int[] propertyIds, in int[] areaIds);
    boolean setPropertiesBatch(in List<PropertyValue> values);

    /**
     * Metadata Operations
     */
    int[] getPropertyIds();
    CarPropertyInfo getPropertyInfo(int propertyId);
    CarPropertyConfig getPropertyConfig(int propertyId);
    boolean isPropertyAvailable(int propertyId, int areaId);
    int[] getSupportedAreas(int propertyId);

    /**
     * Callback Management
     */
    void registerCallback(ICarSettingsCallback callback);
    void unregisterCallback(ICarSettingsCallback callback);
    void registerPropertyListener(int propertyId, IPropertyEventListener listener);
    void unregisterPropertyListener(int propertyId, IPropertyEventListener listener);

    /**
     * Permission Management
     */
    boolean checkPropertyPermission(int propertyId);
    int getRequiredPermissionLevel(int propertyId);

    /**
     * Driver Profile Management
     */
    int getCurrentDriverProfileId();
    boolean switchDriverProfile(int profileId);
    int[] getDriverProfileIds();

    /**
     * Diagnostics
     */
    int getConnectedClientCount();
    String dumpServiceState();
}
