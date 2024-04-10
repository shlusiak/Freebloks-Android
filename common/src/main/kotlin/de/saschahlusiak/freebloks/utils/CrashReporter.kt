package de.saschahlusiak.freebloks.utils

interface CrashReporter {
    fun log(message: String) {}

    fun setString(key: String, value: String) {}

    fun logException(t: Throwable) {}
}

/**
 * Dummy class for crash reporting. Does nothing.
 */
class EmptyCrashReporter: CrashReporter {
    override fun log(message: String) { }

    override fun setString(key: String, value: String) { }

    override fun logException(t: Throwable) { }
}