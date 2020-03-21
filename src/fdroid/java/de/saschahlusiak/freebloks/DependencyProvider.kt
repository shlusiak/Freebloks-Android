package de.saschahlusiak.freebloks

import android.content.Context
import de.saschahlusiak.freebloks.utils.AnalyticsProvider
import de.saschahlusiak.freebloks.utils.CrashReporter
import de.saschahlusiak.freebloks.utils.GooglePlayGamesHelper

object DependencyProvider {
    @JvmStatic
    fun initialise(context: Context) { }

    @JvmStatic
    fun googlePlayGamesHelper() = GooglePlayGamesHelper()

    @JvmStatic
    fun analytics() = AnalyticsProvider()

    @JvmStatic
    fun crashReporter() = CrashReporter()
}

val analytics get() = DependencyProvider.analytics()
val crashReporter get() = DependencyProvider.crashReporter()
