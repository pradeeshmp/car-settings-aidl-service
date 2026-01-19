package com.example.carsettings.aidl;

import com.example.carsettings.aidl.PropertyValue;

oneway interface IPropertyEventListener {
    void onPropertyEvent(int propertyId, int areaId, in PropertyValue value);
}
