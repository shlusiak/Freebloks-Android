package de.saschahlusiak.freebloks;

import android.content.Context;
import android.graphics.Color;

public class Global {
	public final static int VIBRATE_START_DRAGGING = 85;
	public final static int VIBRATE_SET_STONE = 65;
	public final static int VIBRATE_STONE_SNAP = 40;
	
	/* minimum number of starts before rating dialog appears */
	public final static int RATE_MIN_STARTS = 8;
	
	/* minimum elapsed time after first start, before rating dialog appears */
	public static final long RATE_MIN_ELAPSED = 4 * (24 * 60 * 60 * 1000);
	
	/* the default server address: blokus.mooo.com */
	public static final String DEFAULT_SERVER_ADDRESS = "blokus.mooo.com";
	
	
	public final static String base64EncodedPublicKey =
			"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkCGTTo8xtal1NUMNmuPVHLXNY0JtFJgSIkhv2qBEdnSjQugoOdYE2mz6l91QifDArksScX19TXJn5wpuOUayLV88U9O9WJ8T33jYhYkko5ij/nuJu+jBPEvJrKc/jfuAM07Qapk2bSGcbCB8S18PpFjomvPD7dzID8wabhoQdr218XEYfK96CXrOYmHZSs7Yt+bFpM2wAyTNIg/nUv9pfXzpskgpny89HPmChYik5lwOaCAjnO03xfTPV53yUnVwff+ZdBz89uGCMCO1+dj30TAX7KL68dFrb3fnSrxHX8yfUs+5OXQPYQh6PMy6PANDNPoJ68kQiZYRM/W2OIYFgQIDAQAB";

	/* set to true for Amazon export */
	public final static boolean IS_AMAZON = false;
	
	
	public final static String getMarketURLString(Context context) {
		if (IS_AMAZON)
			return "http://www.amazon.com/gp/mas/dl/android?p=" + context.getPackageName();
		else
			return "https://play.google.com/store/apps/details?id=" + context.getPackageName();
	}
	
	
	public static final int PLAYER_BACKGROUND_COLOR[] = {
		Color.rgb(0, 0, 128),
		Color.rgb(140, 140, 0),
		Color.rgb(96, 0, 0),
		Color.rgb(0, 96, 0),
	};
	public static final int PLAYER_FOREGROUND_COLOR[] = {
		Color.rgb(0, 160, 255),
		Color.rgb(255, 255, 0),
		Color.rgb(255, 0, 0),
		Color.rgb(0, 255, 0),
	};
}
