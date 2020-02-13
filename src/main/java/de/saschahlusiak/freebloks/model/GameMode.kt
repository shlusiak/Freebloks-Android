package de.saschahlusiak.freebloks.model

/**
 * This MUST match the order in "jni/constants.h
 */
enum class GameMode {
    GAMEMODE_2_COLORS_2_PLAYERS,
    GAMEMODE_4_COLORS_2_PLAYERS,
    GAMEMODE_4_COLORS_4_PLAYERS,
    GAMEMODE_DUO,
    GAMEMODE_JUNIOR;

    companion object {
        @JvmStatic
		fun from(ordinal: Int): GameMode {
            for (gm in values()) if (gm.ordinal == ordinal) return gm
            throw RuntimeException("Unknown game mode: $ordinal")
        }
    }
}
