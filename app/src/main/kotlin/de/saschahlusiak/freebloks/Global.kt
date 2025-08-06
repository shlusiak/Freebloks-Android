package de.saschahlusiak.freebloks

import java.util.*

object Global {
    /**
     * Is this Freebloks VIP?
     */
    const val IS_VIP = (BuildConfig.FLAVOR_app == "vip")

    /**
     * This this the F-Droid build flavor?
     */
    const val IS_FDROID = (BuildConfig.FLAVOR_store == "fdroid")

    /**
     * This this the Google build flavor?
     */
    const val IS_GOOGLE = (BuildConfig.FLAVOR_store == "google")

    /**
     * This this the Amazon build flavor?
     */
    const val IS_AMAZON = (BuildConfig.FLAVOR_store == "amazon")

    /**
     * Minimum number of starts before rating dialog appears
     */
    const val RATE_MIN_STARTS = 8

    /**
     * Minimum elapsed time after first start, before rating dialog appears (4 days)
     */
    const val RATE_MIN_ELAPSED = 4 * (24 * 60 * 60 * 1000).toLong()

    /**
     * Number of starts before the donate dialog appears / the donate symbol replaces the app icon in the menu
     */
    const val SUPPORT_STARTS = 25

    /**
     * The default server address for Internet play
     */
    const val DEFAULT_SERVER_ADDRESS = "blokus.saschahlusiak.de"

    /**
     * Formats the app store link with the given package name
     */
    fun getMarketURLString(packageName: String) = String.format(Locale.ROOT, BuildConfig.APP_STORE_LINK, packageName)
}