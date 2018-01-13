package de.saschahlusiak.freebloks.controller;

import android.util.Log;
import de.saschahlusiak.freebloks.model.Stone;

public class JNIServer {
	final static String tag = JNIServer.class.getSimpleName();

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


	public static int runServer(Spielleiter spiel, int game_mode, int field_size, int ki_mode) {
		int ki_threads = get_number_of_processors();

		Log.d(tag, "spawning server with " + ki_threads + " threads");

		if (spiel == null)
			return native_run_server(game_mode, field_size, field_size, ki_mode, ki_threads);
		else {
			int player_stones_available[] = new int[Stone.STONE_COUNT_ALL_SHAPES * 4];
			int i, j;

			for (i = 0; i < 4; i++)
				for (j = 0; j < Stone.STONE_COUNT_ALL_SHAPES; j++)
					player_stones_available[i * Stone.STONE_COUNT_ALL_SHAPES + j] = spiel.getPlayer(i).get_stone(j).get_available();

			return native_resume_server(
					spiel.width,
					spiel.height,
					spiel.current_player(),
					spiel.spieler,
					spiel.getFields(),
					player_stones_available,
					game_mode,
					ki_mode,
					ki_threads);
		}
	}

	static {
		Log.d(tag, "loading server.so");
		System.loadLibrary("server");
	}
}
