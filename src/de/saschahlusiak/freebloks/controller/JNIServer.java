package de.saschahlusiak.freebloks.controller;

import de.saschahlusiak.freebloks.model.Spiel;
import de.saschahlusiak.freebloks.model.Stone;

public class JNIServer {
	private static native int native_run_server(int game_mode, int ki_mode);
	
	private static native int native_resume_server(
			int field_size_x,
			int field_size_y,
			int current_player,
			int spieler[],
			int field_data[],
			int player_stone_data[],
			int game_mode,
			int ki_mode);
	
	
	public static void runServer(Spielleiter spiel, int game_mode, int ki_mode) {
		if (spiel == null)
			native_run_server(game_mode, ki_mode);
		else {
			int player_stones_available[] = new int[Stone.STONE_COUNT_ALL_SHAPES * 4];
			int i, j;
			
			for (i = 0; i < 4; i++)
				for (j = 0; j < Stone.STONE_COUNT_ALL_SHAPES; j++)
					player_stones_available[i * Stone.STONE_COUNT_ALL_SHAPES + j] = spiel.get_player(i).get_stone(j).get_available();
	
			native_resume_server(
					spiel.m_field_size_x,
					spiel.m_field_size_y,
					spiel.current_player(),
					spiel.spieler,
					spiel.get_game_field(), 
					player_stones_available,
					game_mode,
					ki_mode);
		}
	}
	
	static {
		System.loadLibrary("server");
	}
}
