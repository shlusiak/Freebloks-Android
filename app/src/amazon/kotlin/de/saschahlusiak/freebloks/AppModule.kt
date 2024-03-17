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
    fun provideAnalytics(impl: FirebaseAnalyticsProvider): AnalyticsProvider = impl

    @Provides
    @Singleton
    fun provideGamesHelper(impl: DefaultGooglePlayGamesHelper): GooglePlayGamesHelper = impl

    @Provides
    @Singleton
    fun crashReporter(app: Application): CrashReporter = CrashlyticsCrashReporter(app, isEnabled = !BuildConfig.DEBUG)

    @Provides
    fun instantAppHandler(impl: GooglePlayInstantAppHandler): InstantAppHelper = impl
}
