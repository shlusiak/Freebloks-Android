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
     * Minimum number of starts before rating dialog appears
     */
    const val RATE_MIN_STARTS = 8

    /**
     * Minimum elapsed time after first start, before rating dialog appears
     */
    const val RATE_MIN_ELAPSED = 4 * (24 * 60 * 60 * 1000).toLong()

    /**
     * Number of starts before the donate dialog appears
     */
    const val DONATE_STARTS = 25

    /**
     * The default server address for Internet play
     */
    const val DEFAULT_SERVER_ADDRESS = "blokus.saschahlusiak.de"

    /**
     * Formats the app store link with the given package name
     */
    fun getMarketURLString(packageName: String) = String.format(Locale.ROOT, BuildConfig.APP_STORE_LINK, packageName)
}