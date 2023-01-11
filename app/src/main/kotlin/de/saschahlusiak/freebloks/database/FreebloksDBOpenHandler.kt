package de.saschahlusiak.freebloks.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class FreebloksDBOpenHandler(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(HighScoreDB.CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        try {
            // nothing yet
        } catch (e: Exception) {
            db.execSQL(HighScoreDB.DROP_TABLE)
            onCreate(db)
        }
    }

    companion object {
        private const val DATABASE_NAME = "freebloks.db"
        private const val DATABASE_VERSION = 1
    }
}