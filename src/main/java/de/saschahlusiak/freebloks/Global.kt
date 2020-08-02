package de.saschahlusiak.freebloks

import java.util.*

object Global {
    const val VIBRATE_START_DRAGGING = 85L
    const val VIBRATE_SET_STONE = 65L
    const val VIBRATE_STONE_SNAP = 40L

    /**
     * Is this Freebloks VIP?
     */
    const val IS_VIP = (BuildConfig.FLAVOR_app == "vip")

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
    const val DONATE_STARTS = 20

    /**
     * The default server address for Internet play
     */
    const val DEFAULT_SERVER_ADDRESS = "blokus.saschahlusiak.de"

    /**
     * Formats the app store link with the given package name
     */
    fun getMarketURLString(packageName: String) = String.format(Locale.ROOT, BuildConfig.APP_STORE_LINK, packageName)
}