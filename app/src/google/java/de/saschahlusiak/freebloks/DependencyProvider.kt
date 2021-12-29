package de.saschahlusiak.freebloks

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.saschahlusiak.freebloks.utils.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AnalyticsModule {
    @Provides
    @Singleton
    fun provideAnalytics(): AnalyticsProvider = DependencyProvider.analytics()

    @Provides
    @Singleton
    fun provideGamesHelper(): GooglePlayGamesHelper = DependencyProvider.googlePlayGamesHelper()

    @Provides
    @Singleton
    fun crashReporter(): CrashReporter = DependencyProvider.crashReporter()
}

object DependencyProvider {
    // defaults to dummy implementations until initialised
    private var gamesHelper: GooglePlayGamesHelper = GooglePlayGamesHelper()
    private var analytics: AnalyticsProvider = AnalyticsProvider()
    private var crashReporter: CrashReporter = CrashReporter()

    private var initialised = false

    fun initialise(context: Context) {
        if (initialised) return

        crashReporter = CrashlyticsCrashReporter()
        analytics = FirebaseAnalyticsProvider(context)
        gamesHelper = DefaultGooglePlayGamesHelper(context.applicationContext)

        initialised = true
    }

    fun googlePlayGamesHelper() = gamesHelper

    fun analytics() = analytics

    fun crashReporter() = crashReporter
}