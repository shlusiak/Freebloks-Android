package de.saschahlusiak.freebloks.database;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FreebloksDBOpenHandler extends SQLiteOpenHelper {
	public static final String DATABASE_NAME = "freebloks.db";
	private static final int DATABASE_VERSION = 1;

	public FreebloksDBOpenHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(HighscoreDB.CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		try {

		} catch (Exception e) {
			db.execSQL(HighscoreDB.DROP_TABLE);
			onCreate(db);
		}
	}
}
