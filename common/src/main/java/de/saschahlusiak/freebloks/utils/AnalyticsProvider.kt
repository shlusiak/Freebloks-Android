package de.saschahlusiak.freebloks.utils

import android.os.Bundle

interface AnalyticsProvider {
    fun logEvent(name: String, args: Bundle? = null)
}

class EmptyAnalyticsProvider: AnalyticsProvider {
    /**
     * Logs an event to our analytics provider, which is a NOP by default.
     */
    override fun logEvent(name: String, args: Bundle?) { }
}