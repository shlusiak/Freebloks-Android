package de.saschahlusiak.freebloks.model

/**
 * This MUST match the order in "jni/constants.h, as well as R.array.game_modes
 */
enum class GameMode(
    /**
     * Number of colors in this game
     */
    val colors: Int
) {
    GAMEMODE_2_COLORS_2_PLAYERS(2),
    GAMEMODE_4_COLORS_2_PLAYERS(4),
    GAMEMODE_4_COLORS_4_PLAYERS(4),
    GAMEMODE_DUO(2),
    GAMEMODE_JUNIOR(2);

    companion object {
        val DEFAULT = GAMEMODE_4_COLORS_4_PLAYERS

		fun from(ordinal: Int): GameMode {
            for (gm in entries) if (gm.ordinal == ordinal) return gm
            throw RuntimeException("Unknown game mode: $ordinal")
        }
    }
}
