package de.saschahlusiak.freebloks;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.Locale;

import de.saschahlusiak.freebloks.model.GameMode;

public class Global {
	public final static long VIBRATE_START_DRAGGING = 85;
	public final static long VIBRATE_SET_STONE = 65;
	public final static long VIBRATE_STONE_SNAP = 40;
	
	/* is this Freebloks VIP? */
	public final static boolean IS_VIP;

	/* minimum number of starts before rating dialog appears */
	public final static int RATE_MIN_STARTS = 8;

	/* minimum elapsed time after first start, before rating dialog appears */
	public static final long RATE_MIN_ELAPSED = 4 * (24 * 60 * 60 * 1000);

	/* number of starts before the donate dialog appears */
	public final static int DONATE_STARTS = 20;

	/* the default server address: blokus.saschahlusiak.de */
	public static final String DEFAULT_SERVER_ADDRESS = "blokus.saschahlusiak.de";


	public static String getMarketURLString(@NonNull String packageName) {
		return String.format(Locale.ROOT, BuildConfig.APP_STORE_LINK, packageName);
	}


	public static final int[] PLAYER_BACKGROUND_COLOR_RESOURCE = {
		R.color.player_background_white,
		R.color.player_background_blue,
		R.color.player_background_yellow,
		R.color.player_background_red,
		R.color.player_background_green,
		R.color.player_background_orange,
		R.color.player_background_purple,
	};

	public static final int[] PLAYER_FOREGROUND_COLOR_RESOURCE = {
		R.color.player_foreground_white,
		R.color.player_foreground_blue,
		R.color.player_foreground_yellow,
		R.color.player_foreground_red,
		R.color.player_foreground_green,
		R.color.player_foreground_orange,
		R.color.player_foreground_purple,
	};

	private final static float[] stone_white = {0.7f, 0.7f, 0.7f, 0};
	private final static float[] stone_red = {0.75f, 0, 0, 0};
	private final static float[] stone_blue = {0.0f, 0.2f, 1.0f, 0};
	private final static float[] stone_green = {0.0f, 0.65f, 0, 0};
	private final static float[] stone_yellow = {0.80f, 0.80f, 0, 0};
	private final static float[] stone_orange = {0.90f, 0.40f, 0.0f, 0};
	private final static float[] stone_purple = {0.40f, 0.0f, 0.80f, 0};
	public final static float[][] stone_color_a = {stone_white, stone_blue, stone_yellow, stone_red, stone_green, stone_orange, stone_purple};

	private final static float[] stone_red_dark = {0.035f, 0, 0, 0};
	private final static float[] stone_blue_dark = {0.0f, 0.004f, 0.035f, 0};
	private final static float[] stone_green_dark = {0.0f, 0.035f, 0, 0};
	private final static float[] stone_yellow_dark = {0.025f, 0.025f, 0, 0};
	private final static float[] stone_orange_dark = {0.040f, 0.020f, 0, 0};
	private final static float[] stone_purple_dark = {0.020f, 0.000f, 0.040f, 0};
	private final static float[] stone_white_dark = {0.04f, 0.04f, 0.04f, 0};
	public final static float[][] stone_shadow_color_a = {stone_white_dark, stone_blue_dark, stone_yellow_dark, stone_red_dark, stone_green_dark, stone_orange_dark, stone_purple_dark};

	/**
	 * Returns the index of the player color in the above PLAYER_BACKGROUND_COLOR_RESOURCE array
	 *
	 * @param player player
	 * @param gameMode game mode
	 * @return the index into above arrays
	 */
	public static int getPlayerColor(int player, GameMode gameMode) {
		if (gameMode == GameMode.GAMEMODE_DUO || gameMode == GameMode.GAMEMODE_JUNIOR) {
			/* player 1 is orange */
			if (player == 0)
				return 5;
			/* player 2 is purple */
			if (player == 2)
				return 6;
		}
		return player + 1;
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
	public static @NonNull String getColorName(@NonNull Context context, int player, GameMode gameMode) {
		int color = getPlayerColor(player, gameMode);
		return context.getResources().getStringArray(R.array.color_names)[color];
	}

	static {
		IS_VIP = (BuildConfig.FLAVOR_app.equals("vip"));
	}
}
