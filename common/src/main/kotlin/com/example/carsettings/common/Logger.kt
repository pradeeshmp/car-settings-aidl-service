package com.example.carsettings.common

import timber.log.Timber

object Logger {
    fun init() {
        if (Timber.treeCount == 0) {
            Timber.plant(Timber.DebugTree())
        }
    }

    fun d(message: String, vararg args: Any?) = Timber.d(message, *args)
    fun i(message: String, vararg args: Any?) = Timber.i(message, *args)
    fun w(message: String, vararg args: Any?) = Timber.w(message, *args)
    fun e(message: String, vararg args: Any?) = Timber.e(message, *args)
    fun e(t: Throwable, message: String, vararg args: Any?) = Timber.e(t, message, *args)
}
