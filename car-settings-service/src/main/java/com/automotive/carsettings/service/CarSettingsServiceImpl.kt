package com.automotive.carsettings.service

import android.os.IBinder
import android.os.RemoteCallbackList
import android.os.RemoteException
import com.automotive.carsettings.aidl.*
import com.automotive.carsettings.service.binder.ClientManager
import com.automotive.carsettings.service.binder.DeathRecipientManager
import com.automotive.carsettings.service.permission.PermissionChecker
import com.automotive.carsettings.service.property.PropertyHalSimulator
import com.automotive.carsettings.service.property.PropertyRegistry
import com.automotive.carsettings.service.property.PropertyStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Implementation of the ICarSettingsService AIDL interface.
 * Handles all property operations, callbacks, and client management.
 */
@Singleton
class CarSettingsServiceImpl @Inject constructor(
    private val propertyStore: PropertyStore,
    private val propertyRegistry: PropertyRegistry,
    private val permissionChecker: PermissionChecker,
    private val clientManager: ClientManager,
    private val deathRecipientManager: DeathRecipientManager,
    private val halSimulator: PropertyHalSimulator
) : ICarSettingsService.Stub() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val lock = ReentrantReadWriteLock()

    // Global callbacks for all property changes
    private val globalCallbacks = RemoteCallbackList<ICarSettingsCallback>()

    // Property-specific listeners
    private val propertyListeners = ConcurrentHashMap<Int, RemoteCallbackList<IPropertyEventListener>>()

    private var currentStatus = ServiceStatus.STATUS_INITIALIZING
    private var currentProfileId = 1 // Default profile

    fun initialize() {
        Timber.d("CarSettingsServiceImpl initialize()")

        // Load default profile
        propertyStore.loadProfile(currentProfileId)

        // Start HAL simulator
        halSimulator.start { propertyId, areaId, value ->
            onPropertyChangedFromHal(propertyId, areaId, value)
        }

        currentStatus = ServiceStatus.STATUS_READY
        notifyServiceStatusChanged(currentStatus)
    }

    fun shutdown() {
        Timber.d("CarSettingsServiceImpl shutdown()")
        currentStatus = ServiceStatus.STATUS_SHUTTING_DOWN
        notifyServiceStatusChanged(currentStatus)

        halSimulator.stop()
        globalCallbacks.kill()
        propertyListeners.values.forEach { it.kill() }
        scope.cancel()
    }

    // Property CRUD Operations

    override fun getProperty(propertyId: Int, areaId: Int): PropertyValue? {
        Timber.d("getProperty(propertyId=$propertyId, areaId=$areaId)")

        if (!permissionChecker.checkPropertyPermission(propertyId)) {
            Timber.w("Permission denied for property $propertyId")
            return null
        }

        return lock.read {
            propertyStore.getProperty(propertyId, areaId)
        }
    }

    override fun setProperty(value: PropertyValue): Boolean {
        Timber.d("setProperty(propertyId=${value.propertyId}, areaId=${value.areaId})")

        if (!permissionChecker.checkPropertyPermission(value.propertyId)) {
            Timber.w("Permission denied for property ${value.propertyId}")
            return false
        }

        val success = lock.write {
            propertyStore.setProperty(value)
        }

        if (success) {
            notifyPropertyChanged(value.propertyId, value.areaId, value)
        }

        return success
    }

    override fun getPropertyWithArea(propertyId: Int, areaId: Int): PropertyValue? {
        return getProperty(propertyId, areaId)
    }

    override fun setPropertyWithArea(propertyId: Int, areaId: Int, value: PropertyValue): Boolean {
        val updatedValue = value.copy(propertyId = propertyId, areaId = areaId)
        return setProperty(updatedValue)
    }

    // Batch Operations

    override fun getPropertiesBatch(propertyIds: IntArray, areaIds: IntArray): List<PropertyValue> {
        Timber.d("getPropertiesBatch(count=${propertyIds.size})")

        if (propertyIds.size != areaIds.size) {
            Timber.e("Property IDs and area IDs size mismatch")
            return emptyList()
        }

        return lock.read {
            propertyIds.indices.mapNotNull { i ->
                if (permissionChecker.checkPropertyPermission(propertyIds[i])) {
                    propertyStore.getProperty(propertyIds[i], areaIds[i])
                } else {
                    null
                }
            }
        }
    }

    override fun setPropertiesBatch(values: List<PropertyValue>): Boolean {
        Timber.d("setPropertiesBatch(count=${values.size})")

        var allSucceeded = true
        lock.write {
            values.forEach { value ->
                if (permissionChecker.checkPropertyPermission(value.propertyId)) {
                    val success = propertyStore.setProperty(value)
                    if (success) {
                        notifyPropertyChanged(value.propertyId, value.areaId, value)
                    } else {
                        allSucceeded = false
                    }
                } else {
                    allSucceeded = false
                }
            }
        }

        return allSucceeded
    }

    // Metadata Operations

    override fun getPropertyIds(): IntArray {
        return propertyRegistry.getAllPropertyIds()
    }

    override fun getPropertyInfo(propertyId: Int): CarPropertyInfo? {
        return propertyRegistry.getPropertyInfo(propertyId)
    }

    override fun getPropertyConfig(propertyId: Int): CarPropertyConfig? {
        return propertyRegistry.getPropertyConfig(propertyId)
    }

    override fun isPropertyAvailable(propertyId: Int, areaId: Int): Boolean {
        return propertyRegistry.isPropertyAvailable(propertyId, areaId)
    }

    override fun getSupportedAreas(propertyId: Int): IntArray {
        return propertyRegistry.getSupportedAreas(propertyId)
    }

    // Callback Management

    override fun registerCallback(callback: ICarSettingsCallback) {
        Timber.d("registerCallback()")

        val callingPid = android.os.Binder.getCallingPid()
        val callingUid = android.os.Binder.getCallingUid()

        globalCallbacks.register(callback)
        clientManager.addClient(callingUid, callingPid)

        deathRecipientManager.linkToDeath(callback.asBinder()) {
            Timber.w("Client died: uid=$callingUid, pid=$callingPid")
            globalCallbacks.unregister(callback)
            clientManager.removeClient(callingUid)
        }

        Timber.d("Client registered: uid=$callingUid, pid=$callingPid")
    }

    override fun unregisterCallback(callback: ICarSettingsCallback) {
        Timber.d("unregisterCallback()")

        val callingUid = android.os.Binder.getCallingUid()

        globalCallbacks.unregister(callback)
        clientManager.removeClient(callingUid)
        deathRecipientManager.unlinkToDeath(callback.asBinder())
    }

    override fun registerPropertyListener(propertyId: Int, listener: IPropertyEventListener) {
        Timber.d("registerPropertyListener(propertyId=$propertyId)")

        val listeners = propertyListeners.getOrPut(propertyId) {
            RemoteCallbackList()
        }

        listeners.register(listener)

        deathRecipientManager.linkToDeath(listener.asBinder()) {
            Timber.w("Property listener died for property $propertyId")
            listeners.unregister(listener)
        }
    }

    override fun unregisterPropertyListener(propertyId: Int, listener: IPropertyEventListener) {
        Timber.d("unregisterPropertyListener(propertyId=$propertyId)")

        propertyListeners[propertyId]?.unregister(listener)
        deathRecipientManager.unlinkToDeath(listener.asBinder())
    }

    // Permission Management

    override fun checkPropertyPermission(propertyId: Int): Boolean {
        return permissionChecker.checkPropertyPermission(propertyId)
    }

    override fun getRequiredPermissionLevel(propertyId: Int): Int {
        return permissionChecker.getRequiredPermissionLevel(propertyId)
    }

    // Driver Profile Management

    override fun getCurrentDriverProfileId(): Int {
        return currentProfileId
    }

    override fun switchDriverProfile(profileId: Int): Boolean {
        Timber.d("switchDriverProfile(profileId=$profileId)")

        if (profileId < 1 || profileId > 5) {
            Timber.e("Invalid profile ID: $profileId")
            return false
        }

        lock.write {
            // Save current profile
            propertyStore.saveProfile(currentProfileId)

            val oldProfileId = currentProfileId
            currentProfileId = profileId

            // Load new profile
            propertyStore.loadProfile(currentProfileId)

            // Notify listeners
            notifyDriverProfileChanged(oldProfileId, currentProfileId)
        }

        return true
    }

    override fun getDriverProfileIds(): IntArray {
        return intArrayOf(1, 2, 3, 4, 5) // Support 5 profiles
    }

    // Diagnostics

    override fun getConnectedClientCount(): Int {
        return clientManager.getConnectedClientCount()
    }

    override fun dumpServiceState(): String {
        val sb = StringBuilder()
        sb.appendLine("=== Car Settings Service State ===")
        sb.appendLine("Status: $currentStatus")
        sb.appendLine("Current Profile: $currentProfileId")
        sb.appendLine("Connected Clients: ${clientManager.getConnectedClientCount()}")
        sb.appendLine("Global Callbacks: ${globalCallbacks.registeredCallbackCount}")
        sb.appendLine("Property Listeners: ${propertyListeners.size}")
        sb.appendLine("\nProperty Store:")
        sb.append(propertyStore.dump())
        return sb.toString()
    }

    // Private helper methods

    private fun onPropertyChangedFromHal(propertyId: Int, areaId: Int, value: PropertyValue) {
        Timber.d("onPropertyChangedFromHal(propertyId=$propertyId, areaId=$areaId)")

        lock.write {
            propertyStore.setProperty(value)
        }

        notifyPropertyChanged(propertyId, areaId, value)
    }

    private fun notifyPropertyChanged(propertyId: Int, areaId: Int, value: PropertyValue) {
        scope.launch {
            // Notify global callbacks
            val callbackCount = globalCallbacks.beginBroadcast()
            try {
                for (i in 0 until callbackCount) {
                    try {
                        globalCallbacks.getBroadcastItem(i)?.onPropertyChanged(propertyId, areaId, value)
                    } catch (e: RemoteException) {
                        Timber.e(e, "Error notifying global callback")
                    }
                }
            } finally {
                globalCallbacks.finishBroadcast()
            }

            // Notify property-specific listeners
            propertyListeners[propertyId]?.let { listeners ->
                val listenerCount = listeners.beginBroadcast()
                try {
                    for (i in 0 until listenerCount) {
                        try {
                            listeners.getBroadcastItem(i)?.onPropertyEvent(propertyId, areaId, value)
                        } catch (e: RemoteException) {
                            Timber.e(e, "Error notifying property listener")
                        }
                    }
                } finally {
                    listeners.finishBroadcast()
                }
            }
        }
    }

    private fun notifyServiceStatusChanged(status: Int) {
        scope.launch {
            val callbackCount = globalCallbacks.beginBroadcast()
            try {
                for (i in 0 until callbackCount) {
                    try {
                        globalCallbacks.getBroadcastItem(i)?.onServiceStatusChanged(status)
                    } catch (e: RemoteException) {
                        Timber.e(e, "Error notifying service status")
                    }
                }
            } finally {
                globalCallbacks.finishBroadcast()
            }
        }
    }

    private fun notifyDriverProfileChanged(oldProfileId: Int, newProfileId: Int) {
        scope.launch {
            val callbackCount = globalCallbacks.beginBroadcast()
            try {
                for (i in 0 until callbackCount) {
                    try {
                        globalCallbacks.getBroadcastItem(i)?.onDriverProfileChanged(oldProfileId, newProfileId)
                    } catch (e: RemoteException) {
                        Timber.e(e, "Error notifying profile change")
                    }
                }
            } finally {
                globalCallbacks.finishBroadcast()
            }
        }
    }
}
