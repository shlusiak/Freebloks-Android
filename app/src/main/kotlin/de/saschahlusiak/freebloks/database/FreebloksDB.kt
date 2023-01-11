package de.saschahlusiak.freebloks.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException

abstract class FreebloksDB(private val context: Context) {
	protected var db: SQLiteDatabase? = null
    private var dbHelper: FreebloksDBOpenHandler? = null

    @Throws(SQLiteException::class)
    fun open() {
        dbHelper = FreebloksDBOpenHandler(context)
        db = dbHelper?.writableDatabase
    }

    fun close() {
        dbHelper?.close()
        dbHelper = null
    }

    fun beginTransaction() {
        db?.beginTransaction()
    }

    fun endTransaction(success: Boolean) {
        if (success) db?.setTransactionSuccessful()
        db?.endTransaction()
    }
}