package de.saschahlusiak.freebloks.utils

import android.content.Context
import android.os.Bundle
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Bridges our [AnalyticsProvider] to FirebaseAnalytics
 */
class FirebaseAnalyticsProvider(context: Context) : AnalyticsProvider {
    init {
        FirebaseApp.initializeApp(context)
    }

    private val analytics = FirebaseAnalytics.getInstance(context)

    override fun logEvent(name: String, args: Bundle?) = analytics.logEvent(name, args)
}