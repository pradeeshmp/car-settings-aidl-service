package com.automotive.carsettings.service.permission

import android.content.Context
import android.content.pm.PackageManager
import android.os.Binder
import com.automotive.carsettings.aidl.CarPropertyInfo
import com.automotive.carsettings.aidl.PropertyConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Checks permissions for property access based on the calling UID.
 */
@Singleton
class PermissionChecker @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val propertyPermissions = ConcurrentHashMap<Int, Int>()

    init {
        initializePermissions()
    }

    /**
     * Check if the calling process has permission to access a property.
     */
    fun checkPropertyPermission(propertyId: Int): Boolean {
        val callingUid = Binder.getCallingUid()
        val requiredLevel = getRequiredPermissionLevel(propertyId)

        return when (requiredLevel) {
            CarPropertyInfo.PERMISSION_NORMAL -> {
                // Normal permissions always granted
                true
            }
            CarPropertyInfo.PERMISSION_PRIVILEGED -> {
                // Check for privileged permission
                checkPermission("com.automotive.carsettings.permission.PRIVILEGED", callingUid)
            }
            CarPropertyInfo.PERMISSION_SYSTEM -> {
                // Check for system permission
                checkPermission("com.automotive.carsettings.permission.SYSTEM", callingUid)
            }
            else -> {
                Timber.w("Unknown permission level $requiredLevel for property $propertyId")
                false
            }
        }
    }

    /**
     * Get the required permission level for a property.
     */
    fun getRequiredPermissionLevel(propertyId: Int): Int {
        return propertyPermissions[propertyId] ?: CarPropertyInfo.PERMISSION_NORMAL
    }

    private fun checkPermission(permission: String, uid: Int): Boolean {
        val result = context.checkPermission(
            permission,
            android.os.Process.myPid(),
            uid
        )

        val granted = result == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            Timber.w("Permission denied: $permission for UID $uid")
        }

        return granted
    }

    private fun initializePermissions() {
        // Most properties are NORMAL permission
        PropertyConstants.getAllPropertyIds().forEach { propertyId ->
            propertyPermissions[propertyId] = CarPropertyInfo.PERMISSION_NORMAL
        }

        // Privileged properties (require signature permission)
        propertyPermissions[PropertyConstants.VEHICLE_DOOR_LOCK] = CarPropertyInfo.PERMISSION_PRIVILEGED
        propertyPermissions[PropertyConstants.VEHICLE_HEADLIGHTS_AUTO] = CarPropertyInfo.PERMISSION_PRIVILEGED
        propertyPermissions[PropertyConstants.VEHICLE_HEADLIGHTS_ON] = CarPropertyInfo.PERMISSION_PRIVILEGED

        // System properties (require signature|privileged permission)
        // None defined in this demo, but would be used for safety-critical properties

        Timber.d("Initialized permissions for ${propertyPermissions.size} properties")
    }
}
