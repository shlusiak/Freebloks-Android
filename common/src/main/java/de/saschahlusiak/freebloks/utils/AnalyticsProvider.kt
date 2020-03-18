package de.saschahlusiak.freebloks.utils

import android.os.Bundle

open class AnalyticsProvider {
    /**
     * Logs an event to our analytics provider, which is a NOP by default.
     */
    open fun logEvent(name: String, args: Bundle? = null) { }
}