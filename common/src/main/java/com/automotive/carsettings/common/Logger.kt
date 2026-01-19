package com.automotive.carsettings.common

import timber.log.Timber

/**
 * Wrapper around Timber for centralized logging configuration.
 */
object Logger {

    fun init(isDebug: Boolean) {
        if (isDebug) {
            Timber.plant(Timber.DebugTree())
        }
    }

    fun d(message: String, vararg args: Any?) {
        Timber.d(message, *args)
    }

    fun i(message: String, vararg args: Any?) {
        Timber.i(message, *args)
    }

    fun w(message: String, vararg args: Any?) {
        Timber.w(message, *args)
    }

    fun e(throwable: Throwable, message: String? = null, vararg args: Any?) {
        if (message != null) {
            Timber.e(throwable, message, *args)
        } else {
            Timber.e(throwable)
        }
    }

    fun wtf(throwable: Throwable, message: String? = null, vararg args: Any?) {
        if (message != null) {
            Timber.wtf(throwable, message, *args)
        } else {
            Timber.wtf(throwable)
        }
    }
}
