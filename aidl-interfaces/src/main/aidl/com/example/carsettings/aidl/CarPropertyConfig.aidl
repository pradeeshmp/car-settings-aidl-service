package com.example.carsettings.aidl;

import com.example.carsettings.aidl.AreaConfig;

parcelable CarPropertyConfig {
    int propertyId;
    int type;
    int[] supportedAreas;
    AreaConfig[] areaConfigs;
}
