package de.saschahlusiak.freebloks.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public abstract class FreebloksDB {
	Context context;
	SQLiteDatabase db;
	FreebloksDBOpenHandler dbHelper;

	public FreebloksDB(Context context) {
		this.context = context;
	}

	public void open() throws SQLiteException {
		db = null;
		dbHelper = new FreebloksDBOpenHandler(context);
		db = dbHelper.getWritableDatabase();
	}

	public void close() {
		if (dbHelper != null)
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
