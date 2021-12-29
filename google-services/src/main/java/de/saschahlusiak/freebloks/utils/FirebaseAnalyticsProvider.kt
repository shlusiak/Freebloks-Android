package de.saschahlusiak.freebloks.utils

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Bridges our [AnalyticsProvider] to FirebaseAnalytics
 */
class FirebaseAnalyticsProvider(context: Context) : AnalyticsProvider {
    private val analytics = FirebaseAnalytics.getInstance(context)

    override fun logEvent(name: String, args: Bundle?) = analytics.logEvent(name, args)
}