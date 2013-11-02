package de.saschahlusiak.freebloks.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class HighscoreDB extends FreebloksDB {
	public static final String TABLE = "highscores";

	public static final String HIGHSCORE_ID = "_id";				/* 0 */
	public static final String GAME_MODE_ID = "gamemode";			/* 1 */
	public static final String POINTS_ID = "points";				/* 2 */
	public static final String STONES_LEFT_ID = "stonesleft";		/* 3 */
	public static final String PLAYER_COLOR_ID = "playercolor";	/* 4 */
	public static final String PLACE_ID = "place";					/* 5 */
	public static final String FLAGS_ID = "flags";					/* 6 */

	/* NEVER CHANGE THE INDEX OF COLUMNS FOR BACKWARDS COMPATIBILITY */
	public static final int HIGHSCORE_INDEX = 0;
	public static final int GAME_MODE_INDEX = 1;
	public static final int POINTS_INDEX = 2;
	public static final int STONES_LEFT_INDEX = 3;
	public static final int PLAYER_COLOR_INDEX = 4;
	public static final int PLACE_INDEX = 5;
	public static final int FLAGS_INDEX = 6;
	
	
	public static final int FLAG_IS_PERFECT = 0x01;
	
	
	/* WARNING: The IDs are used in Cursors to query the colums, for compatibility they should NEVER be changed.
	 * Make sure to ONLY append colums. */
	public static final String CREATE_TABLE = "CREATE TABLE " + TABLE + " (" +
			HIGHSCORE_ID + " INTEGER PRIMARY KEY, " +	/* 0 */
			GAME_MODE_ID + " INTEGER, " +				/* 1 */
			POINTS_ID + " INTEGER, " +					/* 2 */
			STONES_LEFT_ID + " INTEGER, " +				/* 3 */
			PLAYER_COLOR_ID + " INTEGER, " +			/* 4 */ 
			PLACE_ID + " INTEGER, " +					/* 5 */ 
			FLAGS_ID + " INTEGER);";					/* 6 */

	public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE + ";";

	public HighscoreDB(Context context) {
		super(context);
	}

	public long addHighscore(int game_mode,
			int points,
			int stones_left,
			int player_color, 
			int place,
			int flags) {
		ContentValues values = new ContentValues();

		values.put(GAME_MODE_ID, game_mode);
		values.put(POINTS_ID, points);
		values.put(STONES_LEFT_ID, stones_left);
		values.put(PLAYER_COLOR_ID, player_color);
		values.put(PLACE_ID, place);
		values.put(FLAGS_ID, flags);

		return db.insert(TABLE, null, values);
	}

	public boolean clearHighscores() {
		return db.delete(TABLE, null, null) > 0;
	}
	
	public int getTotalNumberOfGames(int game_mode) {
		int count;
		String sql = "SELECT COUNT(*) FROM " + TABLE;
		if (game_mode >= 0)
			sql += " WHERE " + GAME_MODE_ID + " = " + game_mode;
		Cursor c = db.rawQuery(sql, null);
		c.moveToFirst();
		count = c.getInt(0);
		c.close();
		return count;
	}
	
	public int getTotalNumberOfPoints(int game_mode) {
		int count;
		String sql = "SELECT SUM(" + POINTS_ID + ") FROM " + TABLE;
		if (game_mode >= 0)
			sql += " WHERE " + GAME_MODE_ID + " = " + game_mode;
		Cursor c = db.rawQuery(sql, null);
		c.moveToFirst();
		count = c.getInt(0);
		c.close();
		return count;
	}
	
	public int getNumberOfPlace(int game_mode, int place) {
		int count;
		String sql = "SELECT COUNT(*) FROM " + TABLE + " WHERE " + PLACE_ID + " = " + place;
		if (game_mode >= 0)
			sql += " AND " + GAME_MODE_ID + " = " + game_mode;
		Cursor c = db.rawQuery(sql, null);
		c.moveToFirst();
		count = c.getInt(0);
		c.close();
		return count;
	}
	
	public int getNumberOfPerfectGames(int game_mode) {
		int count;
		String sql = "SELECT COUNT(*) FROM " + TABLE + " WHERE " + STONES_LEFT_ID + " = 0";
		if (game_mode >= 0)
			sql += " AND " + GAME_MODE_ID + " = " + game_mode;
		Cursor c = db.rawQuery(sql, null);
		c.moveToFirst();
		count = c.getInt(0);
		c.close();
		return count;
	}
}
