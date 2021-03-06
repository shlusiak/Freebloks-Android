package de.saschahlusiak.freebloks.utils

import android.util.Log

/**
 * Dummy class for crash reporting. Does nothing.
 */
open class CrashReporter {
    open fun log(message: String) { }

    open fun setString(key: String, value: String) { }

    open fun logException(e: Exception) { }
}