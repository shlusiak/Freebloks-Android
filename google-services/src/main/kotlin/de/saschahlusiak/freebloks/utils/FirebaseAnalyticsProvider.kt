package de.saschahlusiak.freebloks.utils

import android.app.Application
import android.content.Context
import android.os.Bundle
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bridges our [AnalyticsProvider] to FirebaseAnalytics
 */
@Singleton
class FirebaseAnalyticsProvider @Inject constructor(
    context: Application
) : AnalyticsProvider {
    init {
        FirebaseApp.initializeApp(context)
    }

    private val analytics = FirebaseAnalytics.getInstance(context)

    override fun logEvent(name: String, args: Bundle?) = analytics.logEvent(name, args)
}