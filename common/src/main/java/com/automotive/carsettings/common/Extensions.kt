package com.automotive.carsettings.common

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.retryWhen
import timber.log.Timber
import java.io.IOException

/**
 * Extension functions for common operations.
 */

/**
 * Retry a Flow with exponential backoff.
 */
fun <T> Flow<T>.retryWithExponentialBackoff(
    maxRetries: Int = 3,
    initialDelayMillis: Long = 1000,
    maxDelayMillis: Long = 10000,
    factor: Double = 2.0
): Flow<T> {
    var currentDelay = initialDelayMillis

    return retryWhen { cause, attempt ->
        if (attempt < maxRetries && cause is IOException) {
            Timber.w("Retry attempt ${attempt + 1} after ${currentDelay}ms due to: ${cause.message}")
            delay(currentDelay)
            currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelayMillis)
            true
        } else {
            false
        }
    }
}

/**
 * Safe string truncation.
 */
fun String.truncate(maxLength: Int, suffix: String = "..."): String {
    return if (length <= maxLength) {
        this
    } else {
        take(maxLength - suffix.length) + suffix
    }
}

/**
 * Format duration in milliseconds to human-readable string.
 */
fun Long.formatDuration(): String {
    val seconds = this / 1000
    val minutes = seconds / 60
    val hours = minutes / 60

    return when {
        hours > 0 -> "${hours}h ${minutes % 60}m"
        minutes > 0 -> "${minutes}m ${seconds % 60}s"
        else -> "${seconds}s"
    }
}
