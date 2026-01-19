package com.automotive.carsettings.service.binder

import android.os.IBinder
import android.os.RemoteException
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages IBinder.DeathRecipient to handle client crashes.
 * Prevents resource leaks when clients die unexpectedly.
 */
@Singleton
class DeathRecipientManager @Inject constructor() {

    private val deathRecipients = ConcurrentHashMap<IBinder, IBinder.DeathRecipient>()

    /**
     * Link to death for a binder, calling the callback when the client dies.
     */
    fun linkToDeath(binder: IBinder, onDeath: () -> Unit) {
        try {
            val recipient = IBinder.DeathRecipient {
                Timber.w("Binder died: $binder")
                onDeath()
                deathRecipients.remove(binder)
            }

            binder.linkToDeath(recipient, 0)
            deathRecipients[binder] = recipient

            Timber.d("Linked to death for binder: $binder")
        } catch (e: RemoteException) {
            Timber.e(e, "Failed to link to death for binder: $binder")
        }
    }

    /**
     * Unlink from death for a binder.
     */
    fun unlinkToDeath(binder: IBinder) {
        deathRecipients.remove(binder)?.let { recipient ->
            try {
                binder.unlinkToDeath(recipient, 0)
                Timber.d("Unlinked from death for binder: $binder")
            } catch (e: Exception) {
                Timber.e(e, "Failed to unlink from death for binder: $binder")
            }
        }
    }

    /**
     * Get the number of active death recipients.
     */
    fun getActiveCount(): Int {
        return deathRecipients.size
    }
}
