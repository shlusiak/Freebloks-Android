package de.saschahlusiak.freebloks

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.saschahlusiak.freebloks.utils.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ConfigModule {
    @Provides
    @Singleton
    fun provideAnalytics(): AnalyticsProvider = EmptyAnalyticsProvider()

    @Provides
    @Singleton
    fun provideGamesHelper(): GooglePlayGamesHelper = EmptyGooglePlayGamesHelper()

    @Provides
    @Singleton
    fun crashReporter(): CrashReporter = EmptyCrashReporter()
}
