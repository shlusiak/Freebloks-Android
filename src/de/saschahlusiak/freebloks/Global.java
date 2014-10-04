package de.saschahlusiak.freebloks;

import de.saschahlusiak.freebloks.controller.Spielleiter;
import android.graphics.Color;

public class Global {
	public final static int VIBRATE_START_DRAGGING = 85;
	public final static int VIBRATE_SET_STONE = 65;
	public final static int VIBRATE_STONE_SNAP = 40;

	/* minimum number of starts before rating dialog appears */
	public final static int RATE_MIN_STARTS = 8;

	/* minimum elapsed time after first start, before rating dialog appears */
	public static final long RATE_MIN_ELAPSED = 4 * (24 * 60 * 60 * 1000);

	/* number of starts before the donate dialog appears */
	public final static int DONATE_STARTS = 50;

	/* the default server address: blokus.mooo.com */
	public static final String DEFAULT_SERVER_ADDRESS = "blokus.mooo.com";


	public final static String base64EncodedPublicKey =
			"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkCGTTo8xtal1NUMNmuPVHLXNY0JtFJgSIkhv2qBEdnSjQugoOdYE2mz6l91QifDArksScX19TXJn5wpuOUayLV88U9O9WJ8T33jYhYkko5ij/nuJu+jBPEvJrKc/jfuAM07Qapk2bSGcbCB8S18PpFjomvPD7dzID8wabhoQdr218XEYfK96CXrOYmHZSs7Yt+bFpM2wAyTNIg/nUv9pfXzpskgpny89HPmChYik5lwOaCAjnO03xfTPV53yUnVwff+ZdBz89uGCMCO1+dj30TAX7KL68dFrb3fnSrxHX8yfUs+5OXQPYQh6PMy6PANDNPoJ68kQiZYRM/W2OIYFgQIDAQAB";

	/* set to true for Amazon export */
	public final static boolean IS_AMAZON = false;


	public final static String getMarketURLString(String packageName) {
		if (IS_AMAZON)
			return "http://www.amazon.com/gp/mas/dl/android?p=" + packageName;
		else
			return "https://play.google.com/store/apps/details?id=" + packageName;
	}


	public static final int PLAYER_BACKGROUND_COLOR[] = {
		Color.rgb(92, 92, 92),	/* white */
		Color.rgb(0, 0, 128),	/* blue */
		Color.rgb(140, 140, 0),	/* yellow */
		Color.rgb(96, 0, 0),	/* red */
		Color.rgb(0, 96, 0),	/* green */
		Color.rgb(170, 96, 24),	/* orange */
		Color.rgb(96, 0, 140)	/* purple */
	};

	public static final int PLAYER_FOREGROUND_COLOR[] = {
		Color.rgb(255, 255, 255),	/* white */
		Color.rgb(0, 160, 255),	/* blue */
		Color.rgb(255, 255, 0),	/* yellow */
		Color.rgb(255, 0, 0),	/* red */
		Color.rgb(0, 255, 0),	/* green */
		Color.rgb(255, 140, 92),/* orange */
		Color.rgb(180, 64, 255),/* purple */
	};

	final static float stone_white[]={0.7f, 0.7f, 0.7f, 0};
	final static float stone_red[]={0.75f, 0, 0, 0};
	final static float stone_blue[]={0.0f, 0.2f, 1.0f, 0};
	final static float stone_green[]={0.0f, 0.65f, 0, 0};
	final static float stone_yellow[]={0.80f, 0.80f, 0, 0};
	final static float stone_orange[]={0.90f, 0.40f, 0.0f, 0};
	final static float stone_purple[]={0.40f, 0.0f, 0.80f, 0};
	public final static float stone_color_a[][] = { stone_white, stone_blue, stone_yellow, stone_red, stone_green, stone_orange, stone_purple };

	final static float stone_red_dark[]={0.035f, 0, 0, 0};
	final static float stone_blue_dark[]={0.0f, 0.004f, 0.035f, 0};
	final static float stone_green_dark[]={0.0f, 0.035f, 0, 0};
	final static float stone_yellow_dark[]={0.025f, 0.025f, 0, 0};
	final static float stone_orange_dark[]={0.040f, 0.020f, 0, 0};
	final static float stone_purple_dark[]={0.020f, 0.000f, 0.040f, 0};
	final static float stone_white_dark[]={0.04f, 0.04f, 0.04f, 0};
	public final static float stone_shadow_color_a[][] = { stone_white_dark, stone_blue_dark, stone_yellow_dark, stone_red_dark, stone_green_dark, stone_orange_dark, stone_purple_dark };

	public final static int getPlayerColor(int player, int game_mode) {
		if (game_mode == Spielleiter.GAMEMODE_DUO) {
			/* player 1 is orange */
			if (player == 0)
				return 5;
			/* player 2 is purple */
			if (player == 2)
				return 6;
		}
		return player + 1;
	}
}
