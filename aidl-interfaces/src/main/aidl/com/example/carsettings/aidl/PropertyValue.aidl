package com.example.carsettings.aidl;

parcelable PropertyValue {
    int propertyId;
    int areaId;
    int type;
    int status;
    long timestampNanos;
    int intValue;
    float floatValue;
    String stringValue;
    boolean booleanValue;
    int[] intArray;
    float[] floatArray;
    byte[] byteArray;
}
