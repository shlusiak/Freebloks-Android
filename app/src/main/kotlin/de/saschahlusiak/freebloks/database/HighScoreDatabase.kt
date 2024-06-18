package de.saschahlusiak.freebloks.database

import de.saschahlusiak.freebloks.database.dao.HighScoreDao
import de.saschahlusiak.freebloks.database.entity.HighScoreEntry
import de.saschahlusiak.freebloks.model.GameMode
import de.saschahlusiak.freebloks.model.PlayerScore
import javax.inject.Inject

class HighScoreDatabase @Inject constructor(
    private val dao: HighScoreDao
) {
    suspend fun add(
        gameMode: GameMode,
        score: PlayerScore
    ) {
        var flags = 0
        if (score.isPerfect) flags = flags or HighScoreEntry.FLAG_PERFECT
        dao.add(
            HighScoreEntry(
                id = null,
                gameMode = gameMode.ordinal,
                points = score.totalPoints,
                stonesLeft = score.stonesLeft,
                playerColor = score.color1,
                place = score.place,
                flags = flags
            )
        )
    }

    suspend fun getTotalNumberOfGames(gameMode: GameMode) =
        dao.getTotalNumberOfGames(gameMode.ordinal)

    suspend fun getNumberOfGoodGames(gameMode: GameMode) =
        dao.getNumberOfGoodGames(gameMode.ordinal)

    suspend fun getNumberOfPerfectGames(gameMode: GameMode) =
        dao.getNumberOfPerfectGames(gameMode.ordinal)

    suspend fun getTotalNumberOfStonesLeft(gameMode: GameMode) =
        dao.getTotalNumberOfStonesLeft(gameMode.ordinal)

    suspend fun getTotalNumberOfPoints(gameMode: GameMode?) =
        dao.getTotalNumberOfPoints(gameMode?.ordinal)

    suspend fun getNumberOfPlace(gameMode: GameMode?, place: Int, color: Int?) =
        dao.getTotalNumberOfPlace(gameMode?.ordinal, place, color)
}