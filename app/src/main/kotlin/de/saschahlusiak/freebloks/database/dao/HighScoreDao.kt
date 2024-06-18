package de.saschahlusiak.freebloks.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import de.saschahlusiak.freebloks.database.entity.HighScoreEntry
import de.saschahlusiak.freebloks.database.entity.HighScoreEntry.Companion.FLAG_PERFECT

@Dao
interface HighScoreDao {
    @Insert
    suspend fun add(entry: HighScoreEntry)

    @Query("DELETE FROM highscores")
    suspend fun clearAll()

    @Query("SELECT * from highscores WHERE (:gameMode IS NULL OR gamemode = :gameMode)")
    suspend fun getAll(gameMode: Int): List<HighScoreEntry>

    @Query("SELECT COUNT(1) FROM highscores WHERE gamemode = :gameMode")
    suspend fun getTotalNumberOfGames(gameMode: Int): Int

    @Query("SELECT COUNT(1) FROM highscores WHERE (:gameMode IS NULL OR gamemode = :gameMode) AND :place = place AND (:color IS NULL OR playercolor = :color)")
    suspend fun getTotalNumberOfPlace(gameMode: Int?, place: Int, color: Int?): Int

    @Query("SELECT SUM(points) FROM highscores WHERE (:gameMode IS NULL OR gamemode = :gameMode)")
    suspend fun getTotalNumberOfPoints(gameMode: Int?): Int

    @Query("SELECT SUM(stonesleft) FROM highscores WHERE gamemode = :gameMode")
    suspend fun getTotalNumberOfStonesLeft(gameMode: Int): Int

    @Query("SELECT COUNT(1) FROM highscores WHERE gamemode = :gameMode AND stonesleft = 0")
    suspend fun getNumberOfGoodGames(gameMode: Int): Int

    @Query("SELECT COUNT(1) FROM highscores WHERE gamemode = :gameMode AND stonesleft = 0 AND flags&$FLAG_PERFECT=$FLAG_PERFECT")
    suspend fun getNumberOfPerfectGames(gameMode: Int): Int
}
