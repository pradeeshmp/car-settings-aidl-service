package com.example.carsettings.aidl;

parcelable CarSettingsError {
    int errorCode;
    String message;
    int propertyId;
    int areaId;
    long timestamp;
}
