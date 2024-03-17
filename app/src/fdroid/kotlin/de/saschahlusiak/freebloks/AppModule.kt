package de.saschahlusiak.freebloks

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.saschahlusiak.freebloks.utils.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideAnalytics(app: Application): AnalyticsProvider = EmptyAnalyticsProvider()

    @Provides
    @Singleton
    fun provideGamesHelper(app: Application): GooglePlayGamesHelper = EmptyGooglePlayGamesHelper()

    @Provides
    @Singleton
    fun crashReporter(): CrashReporter = EmptyCrashReporter()

    @Provides
    fun instantAppHelper(): InstantAppHelper = DefaultInstantAppHelper()
}
