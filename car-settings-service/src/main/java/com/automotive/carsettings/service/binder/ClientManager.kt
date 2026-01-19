package com.automotive.carsettings.service.binder

import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages connected clients for diagnostics and tracking.
 */
@Singleton
class ClientManager @Inject constructor() {

    data class ClientInfo(
        val uid: Int,
        val pid: Int,
        val connectedAtMillis: Long = System.currentTimeMillis()
    )

    private val clients = ConcurrentHashMap<Int, ClientInfo>()

    /**
     * Add a connected client.
     */
    fun addClient(uid: Int, pid: Int) {
        val clientInfo = ClientInfo(uid, pid)
        clients[uid] = clientInfo
        Timber.d("Client added: uid=$uid, pid=$pid")
    }

    /**
     * Remove a disconnected client.
     */
    fun removeClient(uid: Int) {
        clients.remove(uid)?.let {
            val connectedDuration = System.currentTimeMillis() - it.connectedAtMillis
            Timber.d("Client removed: uid=$uid, was connected for ${connectedDuration}ms")
        }
    }

    /**
     * Get the number of currently connected clients.
     */
    fun getConnectedClientCount(): Int {
        return clients.size
    }

    /**
     * Get all connected clients for diagnostics.
     */
    fun getAllClients(): List<ClientInfo> {
        return clients.values.toList()
    }

    /**
     * Get a dump of all clients for diagnostics.
     */
    fun dump(): String {
        val sb = StringBuilder()
        sb.appendLine("Connected Clients (${clients.size}):")
        clients.values.forEach { client ->
            val duration = System.currentTimeMillis() - client.connectedAtMillis
            sb.appendLine("  UID: ${client.uid}, PID: ${client.pid}, Duration: ${duration}ms")
        }
        return sb.toString()
    }
}
