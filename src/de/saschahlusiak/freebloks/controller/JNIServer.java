package de.saschahlusiak.freebloks.controller;

import de.saschahlusiak.freebloks.model.Stone;

public class JNIServer {
	/* unfortunately Runtime.availableProcessors() returns only the number of online cores */
	public static native int get_number_of_processors();
	
	private static native int native_run_server(int game_mode, int field_size_x, int field_size_y, int ki_mode, int ki_threads);
	
	private static native int native_resume_server(
			int field_size_x,
			int field_size_y,
			int current_player,
			int spieler[],
			int field_data[],
			int player_stone_data[],
			int game_mode,
			int ki_mode,
			int ki_threads);
	
	
	public static void runServer(Spielleiter spiel, int game_mode, int field_size, int ki_mode) {
		int ki_threads = get_number_of_processors();
		
		if (spiel == null)
			native_run_server(game_mode, field_size, field_size, ki_mode, ki_threads);
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
					ki_mode,
					ki_threads);
		}
	}
	
	static {
		System.loadLibrary("server");
	}
}
