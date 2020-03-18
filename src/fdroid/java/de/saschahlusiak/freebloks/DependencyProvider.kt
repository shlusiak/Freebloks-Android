package de.saschahlusiak.freebloks

import android.content.Context
import de.saschahlusiak.freebloks.utils.AnalyticsProvider
import de.saschahlusiak.freebloks.utils.GooglePlayGamesHelper

object DependencyProvider {
    @JvmStatic
    fun googlePlayGamesHelper(context: Context) = GooglePlayGamesHelper()

    @JvmStatic
    fun analytics(context: Context) = AnalyticsProvider()
}