package de.saschahlusiak.freebloks

import android.content.Context
import de.saschahlusiak.freebloks.model.GameMode
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

    fun getMarketURLString(packageName: String): String {
        return String.format(Locale.ROOT, BuildConfig.APP_STORE_LINK, packageName)
    }

    val PLAYER_BACKGROUND_COLOR_RESOURCE = intArrayOf(
        R.color.player_background_white,
        R.color.player_background_blue,
        R.color.player_background_yellow,
        R.color.player_background_red,
        R.color.player_background_green,
        R.color.player_background_orange,
        R.color.player_background_purple
    )

    val PLAYER_FOREGROUND_COLOR_RESOURCE = intArrayOf(
        R.color.player_foreground_white,
        R.color.player_foreground_blue,
        R.color.player_foreground_yellow,
        R.color.player_foreground_red,
        R.color.player_foreground_green,
        R.color.player_foreground_orange,
        R.color.player_foreground_purple
    )

    private val stone_white = floatArrayOf(0.7f, 0.7f, 0.7f, 0f)
    private val stone_red = floatArrayOf(0.75f, 0f, 0f, 0f)
    private val stone_blue = floatArrayOf(0.0f, 0.2f, 1.0f, 0f)
    private val stone_green = floatArrayOf(0.0f, 0.65f, 0f, 0f)
    private val stone_yellow = floatArrayOf(0.80f, 0.80f, 0f, 0f)
    private val stone_orange = floatArrayOf(0.90f, 0.40f, 0.0f, 0f)
    private val stone_purple = floatArrayOf(0.40f, 0.0f, 0.80f, 0f)
    val stone_color_a = arrayOf(stone_white, stone_blue, stone_yellow, stone_red, stone_green, stone_orange, stone_purple)

    private val stone_red_dark = floatArrayOf(0.035f, 0f, 0f, 0f)
    private val stone_blue_dark = floatArrayOf(0.0f, 0.004f, 0.035f, 0f)
    private val stone_green_dark = floatArrayOf(0.0f, 0.035f, 0f, 0f)
    private val stone_yellow_dark = floatArrayOf(0.025f, 0.025f, 0f, 0f)
    private val stone_orange_dark = floatArrayOf(0.040f, 0.020f, 0f, 0f)
    private val stone_purple_dark = floatArrayOf(0.020f, 0.000f, 0.040f, 0f)
    private val stone_white_dark = floatArrayOf(0.04f, 0.04f, 0.04f, 0f)
    val stone_shadow_color_a = arrayOf(stone_white_dark, stone_blue_dark, stone_yellow_dark, stone_red_dark, stone_green_dark, stone_orange_dark, stone_purple_dark)

    /**
     * Returns the index of the player color in the above PLAYER_BACKGROUND_COLOR_RESOURCE array
     *
     * @param player player
     * @param gameMode game mode
     * @return the index into above arrays
     */
    fun getPlayerColor(player: Int, gameMode: GameMode): Int {
        if (gameMode === GameMode.GAMEMODE_DUO || gameMode === GameMode.GAMEMODE_JUNIOR) {
            /* player 1 is orange */
            if (player == 0) return 5
            /* player 2 is purple */if (player == 2) return 6
        }

        return player + 1
    }

    /**
     * Returns the name of the color of the given player for the given game mode, e.g. "Blue" or "Orange".
     *
     * @param context context
     * @param player player number
     * @param gameMode game mode
     *
     * @return the name of the color, e.g. "Blue"
     */
    fun getColorName(context: Context, player: Int, gameMode: GameMode): String {
        val color = getPlayerColor(player, gameMode)
        return context.resources.getStringArray(R.array.color_names)[color]
    }
}