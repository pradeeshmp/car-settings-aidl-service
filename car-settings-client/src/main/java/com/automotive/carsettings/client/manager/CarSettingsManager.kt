package com.automotive.carsettings.client.manager

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import com.automotive.carsettings.aidl.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow

/**
 * High-level client API for interacting with the Car Settings Service.
 * Handles service binding, connection management, and provides Flow-based property observation.
 */
@Singleton
class CarSettingsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    sealed class ConnectionState {
        object Disconnected : ConnectionState()
        object Connecting : ConnectionState()
        object Connected : ConnectionState()
        data class Error(val message: String) : ConnectionState()
    }

    data class PropertyChangeEvent(
        val propertyId: Int,
        val areaId: Int,
        val value: PropertyValue
    )

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _propertyChanges = MutableSharedFlow<PropertyChangeEvent>(replay = 0, extraBufferCapacity = 100)
    val propertyChanges: SharedFlow<PropertyChangeEvent> = _propertyChanges.asSharedFlow()

    private val _errors = MutableSharedFlow<CarSettingsError>(replay = 0, extraBufferCapacity = 10)
    val errors: SharedFlow<CarSettingsError> = _errors.asSharedFlow()

    private val _serviceStatus = MutableStateFlow(ServiceStatus.STATUS_INITIALIZING)
    val serviceStatus: StateFlow<Int> = _serviceStatus.asStateFlow()

    private var service: ICarSettingsService? = null
    private val retryCount = AtomicInteger(0)

    private val callback = object : ICarSettingsCallback.Stub() {
        override fun onPropertyChanged(propertyId: Int, areaId: Int, value: PropertyValue) {
            Timber.d("onPropertyChanged: propertyId=$propertyId, areaId=$areaId")
            scope.launch {
                _propertyChanges.emit(PropertyChangeEvent(propertyId, areaId, value))
            }
        }

        override fun onPropertyError(propertyId: Int, areaId: Int, error: CarSettingsError) {
            Timber.w("onPropertyError: propertyId=$propertyId, error=${error.message}")
            scope.launch {
                _errors.emit(error)
            }
        }

        override fun onServiceStatusChanged(status: Int) {
            Timber.d("onServiceStatusChanged: status=$status")
            _serviceStatus.value = status
        }

        override fun onDriverProfileChanged(oldProfileId: Int, newProfileId: Int) {
            Timber.d("onDriverProfileChanged: $oldProfileId -> $newProfileId")
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            Timber.d("Service connected")
            service = ICarSettingsService.Stub.asInterface(binder)
            retryCount.set(0)

            try {
                service?.registerCallback(callback)
                _connectionState.value = ConnectionState.Connected
            } catch (e: RemoteException) {
                Timber.e(e, "Failed to register callback")
                _connectionState.value = ConnectionState.Error("Failed to register callback")
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Timber.w("Service disconnected")
            service = null
            _connectionState.value = ConnectionState.Disconnected

            // Attempt to reconnect
            scope.launch {
                reconnectWithBackoff()
            }
        }

        override fun onBindingDied(name: ComponentName?) {
            Timber.e("Service binding died")
            service = null
            _connectionState.value = ConnectionState.Error("Service binding died")

            scope.launch {
                reconnectWithBackoff()
            }
        }
    }

    /**
     * Connect to the Car Settings Service.
     */
    fun connect() {
        if (_connectionState.value is ConnectionState.Connected ||
            _connectionState.value is ConnectionState.Connecting) {
            Timber.w("Already connected or connecting")
            return
        }

        Timber.d("Connecting to Car Settings Service")
        _connectionState.value = ConnectionState.Connecting

        val intent = Intent().apply {
            action = "com.automotive.carsettings.service.BIND_SERVICE"
            `package` = "com.automotive.carsettings.service"
        }

        try {
            val bound = context.bindService(
                intent,
                serviceConnection,
                Context.BIND_AUTO_CREATE
            )

            if (!bound) {
                Timber.e("Failed to bind service")
                _connectionState.value = ConnectionState.Error("Failed to bind service")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error binding service")
            _connectionState.value = ConnectionState.Error("Error binding service: ${e.message}")
        }
    }

    /**
     * Disconnect from the Car Settings Service.
     */
    fun disconnect() {
        Timber.d("Disconnecting from Car Settings Service")

        try {
            service?.unregisterCallback(callback)
        } catch (e: RemoteException) {
            Timber.e(e, "Error unregistering callback")
        }

        try {
            context.unbindService(serviceConnection)
        } catch (e: Exception) {
            Timber.e(e, "Error unbinding service")
        }

        service = null
        _connectionState.value = ConnectionState.Disconnected
    }

    /**
     * Get an integer property value.
     */
    suspend fun getIntProperty(propertyId: Int, areaId: Int = AreaConstants.AREA_GLOBAL): Result<Int> {
        return executeServiceCall {
            service?.getProperty(propertyId, areaId)?.intValue
                ?: throw IllegalStateException("Property not found")
        }
    }

    /**
     * Set an integer property value.
     */
    suspend fun setIntProperty(propertyId: Int, areaId: Int = AreaConstants.AREA_GLOBAL, value: Int): Result<Boolean> {
        return executeServiceCall {
            val propertyValue = PropertyValue.forInt(propertyId, areaId, value)
            service?.setProperty(propertyValue) ?: false
        }
    }

    /**
     * Get a float property value.
     */
    suspend fun getFloatProperty(propertyId: Int, areaId: Int = AreaConstants.AREA_GLOBAL): Result<Float> {
        return executeServiceCall {
            service?.getProperty(propertyId, areaId)?.floatValue
                ?: throw IllegalStateException("Property not found")
        }
    }

    /**
     * Set a float property value.
     */
    suspend fun setFloatProperty(propertyId: Int, areaId: Int = AreaConstants.AREA_GLOBAL, value: Float): Result<Boolean> {
        return executeServiceCall {
            val propertyValue = PropertyValue.forFloat(propertyId, areaId, value)
            service?.setProperty(propertyValue) ?: false
        }
    }

    /**
     * Get a boolean property value.
     */
    suspend fun getBooleanProperty(propertyId: Int, areaId: Int = AreaConstants.AREA_GLOBAL): Result<Boolean> {
        return executeServiceCall {
            service?.getProperty(propertyId, areaId)?.booleanValue
                ?: throw IllegalStateException("Property not found")
        }
    }

    /**
     * Set a boolean property value.
     */
    suspend fun setBooleanProperty(propertyId: Int, areaId: Int = AreaConstants.AREA_GLOBAL, value: Boolean): Result<Boolean> {
        return executeServiceCall {
            val propertyValue = PropertyValue.forBoolean(propertyId, areaId, value)
            service?.setProperty(propertyValue) ?: false
        }
    }

    /**
     * Get a string property value.
     */
    suspend fun getStringProperty(propertyId: Int, areaId: Int = AreaConstants.AREA_GLOBAL): Result<String> {
        return executeServiceCall {
            service?.getProperty(propertyId, areaId)?.stringValue
                ?: throw IllegalStateException("Property not found")
        }
    }

    /**
     * Set a string property value.
     */
    suspend fun setStringProperty(propertyId: Int, areaId: Int = AreaConstants.AREA_GLOBAL, value: String): Result<Boolean> {
        return executeServiceCall {
            val propertyValue = PropertyValue.forString(propertyId, areaId, value)
            service?.setProperty(propertyValue) ?: false
        }
    }

    /**
     * Observe a property for changes.
     */
    fun observeProperty(propertyId: Int, areaId: Int = AreaConstants.AREA_GLOBAL): Flow<PropertyValue> = callbackFlow {
        val listener = object : IPropertyEventListener.Stub() {
            override fun onPropertyEvent(propId: Int, aId: Int, value: PropertyValue) {
                if (propId == propertyId && (areaId == AreaConstants.AREA_GLOBAL || aId == areaId)) {
                    trySend(value)
                }
            }
        }

        try {
            service?.registerPropertyListener(propertyId, listener)

            // Emit initial value
            service?.getProperty(propertyId, areaId)?.let { trySend(it) }

            awaitClose {
                try {
                    service?.unregisterPropertyListener(propertyId, listener)
                } catch (e: Exception) {
                    Timber.e(e, "Error unregistering property listener")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error registering property listener")
            close(e)
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Get all property IDs.
     */
    suspend fun getAllPropertyIds(): Result<IntArray> {
        return executeServiceCall {
            service?.propertyIds ?: intArrayOf()
        }
    }

    /**
     * Switch driver profile.
     */
    suspend fun switchDriverProfile(profileId: Int): Result<Boolean> {
        return executeServiceCall {
            service?.switchDriverProfile(profileId) ?: false
        }
    }

    /**
     * Get service state dump for debugging.
     */
    suspend fun dumpServiceState(): Result<String> {
        return executeServiceCall {
            service?.dumpServiceState() ?: "Service not connected"
        }
    }

    fun cleanup() {
        disconnect()
        scope.cancel()
    }

    private suspend fun <T> executeServiceCall(block: suspend () -> T): Result<T> {
        return try {
            if (service == null) {
                Result.failure(IllegalStateException("Service not connected"))
            } else {
                Result.success(block())
            }
        } catch (e: RemoteException) {
            Timber.e(e, "RemoteException during service call")
            Result.failure(e)
        } catch (e: Exception) {
            Timber.e(e, "Exception during service call")
            Result.failure(e)
        }
    }

    private suspend fun reconnectWithBackoff() {
        val attempt = retryCount.incrementAndGet()
        if (attempt > 5) {
            Timber.e("Max reconnection attempts reached")
            _connectionState.value = ConnectionState.Error("Max reconnection attempts reached")
            return
        }

        val delayMs = (1000 * 2.0.pow(attempt - 1)).toLong().coerceAtMost(30000)
        Timber.d("Reconnecting in ${delayMs}ms (attempt $attempt)")

        delay(delayMs)
        connect()
    }
}
