package de.saschahlusiak.freebloks

import android.content.Context
import de.saschahlusiak.freebloks.utils.AnalyticsProvider
import de.saschahlusiak.freebloks.utils.CrashReporter
import de.saschahlusiak.freebloks.utils.GooglePlayGamesHelper

@Suppress("UNUSED_PARAMETER")
object DependencyProvider {
    fun initialise(context: Context) { }

    fun googlePlayGamesHelper() = GooglePlayGamesHelper()

    fun analytics() = AnalyticsProvider()

    fun crashReporter() = CrashReporter()
}

val analytics get() = DependencyProvider.analytics()
val crashReporter get() = DependencyProvider.crashReporter()
fun Exception.logException() = crashReporter.logException(this)