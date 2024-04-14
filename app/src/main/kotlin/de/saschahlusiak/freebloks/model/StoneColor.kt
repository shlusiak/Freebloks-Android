package de.saschahlusiak.freebloks.model

import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.theme.*

/**
 * The definition of all possible colors for all stones
 */
@Immutable
enum class StoneColor(
    val backgroundColor: Color,
    val foregroundColor: Color,
    val stoneColor: FloatArray,
    val shadowColor: FloatArray,
    @StringRes val labelResId: Int
) {
    White(
        playerBackgroundWhite,
        playerForegroundWhite,
        floatArrayOf(0.75f, 0.75f, 0.75f, 0f),
        floatArrayOf(0.04f, 0.04f, 0.04f, 0f),
        R.string.white
    ),
    Blue(
        playerBackgroundBlue,
        playerForegroundBlue,
        floatArrayOf(0.0f, 0.22f, 1.0f, 0f),
        floatArrayOf(0.0f, 0.004f, 0.035f, 0f),
        R.string.blue
    ),
    Yellow(
        playerBackgroundYellow,
        playerForegroundYellow,
        floatArrayOf(0.95f, 0.95f, 0.0f, 0f),
        floatArrayOf(0.025f, 0.025f, 0f, 0f),
        R.string.yellow
    ),
    Red(
        playerBackgroundRed,
        playerForegroundRed,
        floatArrayOf(0.80f, 0f, 0f, 0f),
        floatArrayOf(0.035f, 0f, 0f, 0f),
        R.string.red
    ),
    Green(
        playerBackgroundGreen,
        playerForegroundGreen,
        floatArrayOf(0.0f, 0.75f, 0.0f, 0f),
        floatArrayOf(0.0f, 0.035f, 0f, 0f),
        R.string.green
    ),
    Orange(
        playerBackgroundOrange,
        playerForegroundOrange,
        floatArrayOf(0.95f, 0.45f, 0.0f, 0f),
        floatArrayOf(0.040f, 0.020f, 0f, 0f),
        R.string.orange
    ),
    Purple(
        playerBackgroundPurple,
        playerForegroundPurple,
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

            return when (player) {
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


@Stable
fun GameMode.stoneColors() = when (this) {
    GameMode.GAMEMODE_2_COLORS_2_PLAYERS -> listOf(
        StoneColor.Blue,
        StoneColor.Red
    )

    GameMode.GAMEMODE_DUO,
    GameMode.GAMEMODE_JUNIOR -> listOf(
        StoneColor.Orange,
        StoneColor.Purple
    )

    GameMode.GAMEMODE_4_COLORS_2_PLAYERS,
    GameMode.GAMEMODE_4_COLORS_4_PLAYERS -> listOf(
        StoneColor.Blue,
        StoneColor.Yellow,
        StoneColor.Red,
        StoneColor.Green
    )
}
