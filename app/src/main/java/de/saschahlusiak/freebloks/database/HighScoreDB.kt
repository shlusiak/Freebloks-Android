package de.saschahlusiak.freebloks.database

import android.content.ContentValues
import android.content.Context
import de.saschahlusiak.freebloks.model.GameMode

class HighScoreDB(context: Context) : FreebloksDB(context) {
    fun addHighScore(
        gameMode: GameMode,
        points: Int,
        stonesLeft: Int,
        playerColor: Int,
        place: Int,
        flags: Int
    ) {
        val values = ContentValues().apply {
            put(GAME_MODE_ID, gameMode.ordinal)
            put(POINTS_ID, points)
            put(STONES_LEFT_ID, stonesLeft)
            put(PLAYER_COLOR_ID, playerColor)
            put(PLACE_ID, place)
            put(FLAGS_ID, flags)
        }

        db?.insert(TABLE, null, values)
    }

    fun clearHighScores() {
        db?.delete(TABLE, null, null)
    }

    fun getTotalNumberOfGames(gameMode: GameMode?): Int {
        var sql = "SELECT COUNT(*) FROM $TABLE"
        if (gameMode != null) sql += " WHERE $GAME_MODE_ID = ${gameMode.ordinal}"
        return db?.rawQuery(sql, null)?.use { c ->
            c.moveToFirst()
            c.getInt(0)
        } ?: 0
    }

    fun getTotalNumberOfPoints(game_mode: GameMode?): Int {
        var sql = "SELECT SUM($POINTS_ID) FROM $TABLE"
        if (game_mode != null) sql += " WHERE " + GAME_MODE_ID + " = " + game_mode.ordinal
        return db?.rawQuery(sql, null)?.use { c ->
            c.moveToFirst()
            c.getInt(0)
        } ?: 0
    }

    fun getTotalNumberOfStonesLeft(gameMode: GameMode?): Int {
        var sql = "SELECT SUM($STONES_LEFT_ID) FROM $TABLE"
        if (gameMode != null) sql += " WHERE " + GAME_MODE_ID + " = " + gameMode.ordinal
        return db?.rawQuery(sql, null)?.use { c ->
            c.moveToFirst()
            c.getInt(0)
        } ?: 0
    }

    fun getNumberOfPlace(gameMode: GameMode?, place: Int): Int {
        var sql = "SELECT COUNT(*) FROM $TABLE WHERE $PLACE_ID = $place"
        if (gameMode != null) sql += " AND " + GAME_MODE_ID + " = " + gameMode.ordinal
        return db?.rawQuery(sql, null)?.use { c ->
            c.moveToFirst()
            c.getInt(0)
        } ?: 0
    }

    fun getNumberOfPlace(gameMode: GameMode?, place: Int, color: Int): Int {
        var sql = "SELECT COUNT(*) FROM $TABLE WHERE $PLACE_ID = $place AND $PLAYER_COLOR_ID = $color"
        if (gameMode != null) sql += " AND " + GAME_MODE_ID + " = " + gameMode.ordinal
        return db?.rawQuery(sql, null)?.use { c ->
            c.moveToFirst()
            c.getInt(0)
        } ?: 0
    }

    fun getNumberOfGoodGames(gameMode: GameMode?): Int {
        var sql = "SELECT COUNT(*) FROM $TABLE WHERE $STONES_LEFT_ID = 0"
        if (gameMode != null) sql += " AND " + GAME_MODE_ID + " = " + gameMode.ordinal
        return db?.rawQuery(sql, null)?.use { c ->
            c.moveToFirst()
            c.getInt(0)
        } ?: 0
    }

    fun getNumberOfPerfectGames(game_mode: GameMode?): Int {
        var sql = "SELECT COUNT(*) FROM $TABLE WHERE $STONES_LEFT_ID = 0"
        sql += " AND ($FLAGS_ID&$FLAG_IS_PERFECT)=$FLAG_IS_PERFECT"
        if (game_mode != null) sql += " AND " + GAME_MODE_ID + " = " + game_mode.ordinal
        return db?.rawQuery(sql, null)?.use { c ->
            c.moveToFirst()
            c.getInt(0)
        } ?: 0
    }

    companion object {
        private const val TABLE = "highscores"
        private const val HIGHSCORE_ID = "_id" /* 0 */
        private const val GAME_MODE_ID = "gamemode" /* 1 */
        private const val POINTS_ID = "points" /* 2 */
        private const val STONES_LEFT_ID = "stonesleft" /* 3 */
        private const val PLAYER_COLOR_ID = "playercolor" /* 4 */
        private const val PLACE_ID = "place" /* 5 */
        private const val FLAGS_ID = "flags" /* 6 */

        const val FLAG_IS_PERFECT = 0x01

        /* WARNING: The IDs are used in Cursors to query the colums, for compatibility they should NEVER be changed.
	    * Make sure to ONLY append columns. */
        const val CREATE_TABLE = """
            CREATE TABLE $TABLE (
                $HIGHSCORE_ID INTEGER PRIMARY KEY,
                $GAME_MODE_ID INTEGER, 
                $POINTS_ID INTEGER, 
                $STONES_LEFT_ID INTEGER, 
                $PLAYER_COLOR_ID INTEGER, 
                $PLACE_ID INTEGER, 
                $FLAGS_ID INTEGER)
            ;""" /* 6 */

        const val DROP_TABLE = "DROP TABLE IF EXISTS $TABLE;"
    }
}