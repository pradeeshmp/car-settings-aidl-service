package com.example.carsettings.service

import android.os.RemoteCallbackList
import com.example.carsettings.aidl.*
import com.example.carsettings.constants.ServiceStatus
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CarSettingsServiceImpl @Inject constructor(
    private val propertyManager: PropertyManager
) : ICarSettingsService.Stub() {

    private val callbacks = RemoteCallbackList<ICarSettingsCallback>()
    private val propertyListeners = RemoteCallbackList<IPropertyEventListener>()
    private var currentProfileId = 1
    private val profileIds = intArrayOf(1, 2, 3)

    override fun getProperty(propertyId: Int, areaId: Int): PropertyValue {
        Timber.d("getProperty: propertyId=0x${Integer.toHexString(propertyId)}, areaId=$areaId")
        return propertyManager.getPropertyValue(propertyId, areaId) ?: throw IllegalArgumentException("Property not found")
    }

    override fun setProperty(value: PropertyValue) {
        Timber.d("setProperty: propertyId=0x${Integer.toHexString(value.propertyId)}, areaId=${value.areaId}")
        propertyManager.updateValue(value)
        notifyPropertyChanged(value)
    }

    override fun getPropertyWithArea(propertyId: Int, areaId: Int): PropertyValue {
        return getProperty(propertyId, areaId)
    }

    override fun setPropertyWithArea(propertyId: Int, areaId: Int, value: PropertyValue) {
        value.propertyId = propertyId
        value.areaId = areaId
        setProperty(value)
    }

    override fun getPropertiesBatch(propertyIds: IntArray, areaIds: IntArray): Array<PropertyValue> {
        return propertyIds.indices.map { i ->
            getProperty(propertyIds[i], areaIds[i])
        }.toTypedArray()
    }

    override fun setPropertiesBatch(values: Array<PropertyValue>) {
        values.forEach { setProperty(it) }
    }

    override fun getPropertyIds(): IntArray {
        return propertyManager.getPropertyIds()
    }

    override fun getPropertyInfo(propertyId: Int): CarPropertyInfo? {
        // Simple implementation for now
        return CarPropertyInfo().apply {
            this.propertyId = propertyId
        }
    }

    override fun getPropertyConfig(propertyId: Int): CarPropertyConfig? {
        return propertyManager.getPropertyConfig(propertyId)
    }

    override fun isPropertyAvailable(propertyId: Int, areaId: Int): Boolean {
        return propertyManager.isPropertyAvailable(propertyId, areaId)
    }

    override fun getSupportedAreas(propertyId: Int): IntArray {
        return propertyManager.getPropertyConfig(propertyId)?.supportedAreas ?: intArrayOf()
    }

    override fun registerCallback(callback: ICarSettingsCallback) {
        callbacks.register(callback)
        callback.onServiceStatusChanged(ServiceStatus.CONNECTED)
    }

    override fun unregisterCallback(callback: ICarSettingsCallback) {
        callbacks.unregister(callback)
    }

    override fun registerPropertyListener(propertyId: Int, listener: IPropertyEventListener) {
        propertyListeners.register(listener, propertyId)
    }

    override fun unregisterPropertyListener(propertyId: Int, listener: IPropertyEventListener) {
        propertyListeners.unregister(listener)
    }

    override fun checkPropertyPermission(propertyId: Int, permissionLevel: Int): Boolean {
        return true // For demo purposes
    }

    override fun getRequiredPermissionLevel(propertyId: Int): Int {
        return 1
    }

    override fun getCurrentDriverProfileId(): Int {
        return currentProfileId
    }

    override fun switchDriverProfile(profileId: Int) {
        if (profileId in profileIds) {
            val oldId = currentProfileId
            currentProfileId = profileId
            notifyDriverProfileChanged(oldId, profileId)
        }
    }

    override fun getDriverProfileIds(): IntArray {
        return profileIds
    }

    override fun getConnectedClientCount(): Int {
        return callbacks.registeredCallbackCount
    }

    override fun dumpServiceState(): String {
        return "CarSettingsService: ${getConnectedClientCount()} clients connected"
    }

    private fun notifyPropertyChanged(value: PropertyValue) {
        val n = callbacks.beginBroadcast()
        for (i in 0 until n) {
            try {
                callbacks.getBroadcastItem(i).onPropertyChanged(value.propertyId, value.areaId, value)
            } catch (e: Exception) {
                Timber.e(e, "Error notifying callback")
            }
        }
        callbacks.finishBroadcast()

        val m = propertyListeners.beginBroadcast()
        for (i in 0 until m) {
            val registeredPropertyId = propertyListeners.getBroadcastCookie(i) as Int
            if (registeredPropertyId == value.propertyId) {
                try {
                    propertyListeners.getBroadcastItem(i).onPropertyEvent(value.propertyId, value.areaId, value)
                } catch (e: Exception) {
                    Timber.e(e, "Error notifying property listener")
                }
            }
        }
        propertyListeners.finishBroadcast()
    }

    private fun notifyDriverProfileChanged(oldId: Int, newId: Int) {
        val n = callbacks.beginBroadcast()
        for (i in 0 until n) {
            try {
                callbacks.getBroadcastItem(i).onDriverProfileChanged(oldId, newId)
            } catch (e: Exception) {
                Timber.e(e, "Error notifying callback")
            }
        }
        callbacks.finishBroadcast()
    }
}
