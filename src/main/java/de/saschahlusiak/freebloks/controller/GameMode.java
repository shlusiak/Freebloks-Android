package de.saschahlusiak.freebloks.controller;

public enum GameMode {
	GAMEMODE_2_COLORS_2_PLAYERS,
	GAMEMODE_4_COLORS_2_PLAYERS,
	GAMEMODE_4_COLORS_4_PLAYERS,
	GAMEMODE_DUO,
	GAMEMODE_JUNIOR;
	
	public static GameMode from(int ordinal) {
		for (GameMode gm: GameMode.values())
			if (gm.ordinal() == ordinal)
				return gm;
		throw new RuntimeException("Unknown game mode: " + ordinal);
	}
}
