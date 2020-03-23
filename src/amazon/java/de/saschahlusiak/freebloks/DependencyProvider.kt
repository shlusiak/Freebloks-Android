package de.saschahlusiak.freebloks

import android.content.Context
import de.saschahlusiak.freebloks.utils.*

object DependencyProvider {
    private lateinit var gamesHelper: GooglePlayGamesHelper
    private lateinit var analytics: AnalyticsProvider
    private lateinit var crashReporter: CrashReporter

    @JvmStatic
    fun initialise(context: Context) {
        crashReporter = CrashlyticsCrashReporter()
        analytics = FirebaseAnalyticsProvider(context)
        gamesHelper = DefaultGooglePlayGamesHelper(context.applicationContext)
    }

    @Synchronized
    fun googlePlayGamesHelper() = gamesHelper

    @JvmStatic
    fun analytics() = analytics

    @JvmStatic
    fun crashReporter() = crashReporter
}

val analytics get() = DependencyProvider.analytics()
val crashReporter get() = DependencyProvider.crashReporter()
