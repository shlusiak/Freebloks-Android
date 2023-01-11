package de.saschahlusiak.freebloks.model

import android.content.res.Resources
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import de.saschahlusiak.freebloks.R

/**
 * The definition of all possible colors for all stones
 */
enum class StoneColor(
    @ColorRes val backgroundColorId: Int,
    @ColorRes val foregroundColorId: Int,
    val stoneColor: FloatArray,
    val shadowColor: FloatArray,
    @StringRes val labelResId: Int
) {
    White(
        R.color.player_background_white,
        R.color.player_foreground_white,
        floatArrayOf(0.75f, 0.75f, 0.75f, 0f),
        floatArrayOf(0.04f, 0.04f, 0.04f, 0f),
        R.string.white
    ),
    Blue(
        R.color.player_background_blue,
        R.color.player_foreground_blue,
        floatArrayOf(0.0f, 0.22f, 1.0f, 0f),
        floatArrayOf(0.0f, 0.004f, 0.035f, 0f),
        R.string.blue
    ),
    Yellow(
        R.color.player_background_yellow,
        R.color.player_foreground_yellow,
        floatArrayOf(0.95f, 0.95f, 0.0f, 0f),
        floatArrayOf(0.025f, 0.025f, 0f, 0f),
        R.string.yellow
    ),
    Red(
        R.color.player_background_red,
        R.color.player_foreground_red,
        floatArrayOf(0.80f, 0f, 0f, 0f),
        floatArrayOf(0.035f, 0f, 0f, 0f),
        R.string.red
    ),
    Green(
        R.color.player_background_green,
        R.color.player_foreground_green,
        floatArrayOf(0.0f, 0.75f, 0.0f, 0f),
        floatArrayOf(0.0f, 0.035f, 0f, 0f),
        R.string.green
    ),
    Orange(
        R.color.player_background_orange,
        R.color.player_foreground_orange,
        floatArrayOf(0.95f, 0.45f, 0.0f, 0f),
        floatArrayOf(0.040f, 0.020f, 0f, 0f),
        R.string.orange
    ),
    Purple(
        R.color.player_background_purple,
        R.color.player_foreground_purple,
        floatArrayOf(0.45f, 0.0f, 0.90f, 0f),
        floatArrayOf(0.020f, 0.000f, 0.040f, 0f),
        R.string.purple
    );

    fun getName(res: Resources) = res.getString(labelResId)

    companion object {
        fun of(player: Int, gameMode: GameMode): StoneColor {
            if (gameMode == GameMode.GAMEMODE_DUO || gameMode == GameMode.GAMEMODE_JUNIOR) {
                if (player == 0) return Orange
                if (player == 2) return Purple
            }

            return when(player) {
                0 -> Blue
                1 -> Yellow
                2 -> Red
                3 -> Green
                else -> White
            }
        }
    }
}

fun GameMode.colorOf(player: Int) = StoneColor.of(player, this)
fun Game.colorOf(player: Int) = StoneColor.of(player, gameMode)