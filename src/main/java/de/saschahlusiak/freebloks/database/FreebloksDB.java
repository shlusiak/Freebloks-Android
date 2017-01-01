package de.saschahlusiak.freebloks.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public abstract class FreebloksDB {
	Context context;
	SQLiteDatabase db;
	FreebloksDBOpenHandler dbHelper;

	public FreebloksDB(Context context) {
		this.context = context;
	}

	public boolean open() {
		db = null;
		dbHelper = new FreebloksDBOpenHandler(context);
		try {
			db = dbHelper.getWritableDatabase();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return (db != null);
	}

	public void close() {
		dbHelper.close();
	}

	public void beginTransaction() {
		db.beginTransaction();
	}

	public void endTransaction(boolean success) {
		if (success)
			db.setTransactionSuccessful();

		db.endTransaction();
	}
}
