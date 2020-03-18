package de.saschahlusiak.freebloks

import android.content.Context
import de.saschahlusiak.freebloks.utils.DefaultGooglePlayGamesHelper
import de.saschahlusiak.freebloks.utils.FirebaseAnalyticsProvider
import de.saschahlusiak.freebloks.utils.GooglePlayGamesHelper
import java.lang.ref.WeakReference

object DependencyProvider {
    private var gamesHelper: WeakReference<GooglePlayGamesHelper>? = null

    @Synchronized
    fun googlePlayGamesHelper(context: Context): GooglePlayGamesHelper {
        return gamesHelper?.get() ?: DefaultGooglePlayGamesHelper(context.applicationContext).also {
            gamesHelper = WeakReference(it)
        }
    }

    @JvmStatic
    fun analytics(context: Context) = FirebaseAnalyticsProvider(context)
}