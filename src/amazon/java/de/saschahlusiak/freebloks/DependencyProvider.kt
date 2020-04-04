package de.saschahlusiak.freebloks

import android.content.Context
import de.saschahlusiak.freebloks.utils.*

object DependencyProvider {
    private lateinit var gamesHelper: GooglePlayGamesHelper
    private lateinit var analytics: AnalyticsProvider
    private lateinit var crashReporter: CrashReporter

    private var initialised = false

    @JvmStatic
    fun initialise(context: Context) {
        if (initialised) return

        crashReporter = CrashlyticsCrashReporter()
        analytics = FirebaseAnalyticsProvider(context)
        gamesHelper = DefaultGooglePlayGamesHelper(context.applicationContext)

        initialised = true
    }

    fun googlePlayGamesHelper() = gamesHelper

    @JvmStatic
    fun analytics() = analytics

    @JvmStatic
    fun crashReporter() = crashReporter
}

val gamesHelper get() = DependencyProvider.googlePlayGamesHelper()
val analytics get() = DependencyProvider.analytics()
val crashReporter get() = DependencyProvider.crashReporter()
