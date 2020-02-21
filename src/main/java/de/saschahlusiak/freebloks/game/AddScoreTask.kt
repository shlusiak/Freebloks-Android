package de.saschahlusiak.freebloks.game

import android.content.Context
import android.database.sqlite.SQLiteException
import com.crashlytics.android.Crashlytics
import de.saschahlusiak.freebloks.database.HighscoreDB
import de.saschahlusiak.freebloks.model.GameMode
import de.saschahlusiak.freebloks.model.PlayerScore

class AddScoreTask(private val context: Context, private val gameMode: GameMode, private val scores: Array<PlayerScore>) : Thread() {
    override fun run() {
        val db = HighscoreDB(context)

        try {
            db.open()

            scores
                .filter { it.isLocal }
                .forEach { score ->
                    var flags = 0
                    if (score.isPerfect) flags = flags or HighscoreDB.FLAG_IS_PERFECT

                    db.addHighscore(
                        gameMode,
                        score.totalPoints,
                        score.stonesLeft,
                        score.color1,
                        score.place,
                        flags
                    )

                }

            db.close()
        } catch (e: SQLiteException) {
            Crashlytics.logException(e)
            e.printStackTrace()
        }
    }

}