package de.saschahlusiak.freebloks.game.finish

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteException
import android.os.Bundle
import androidx.annotation.WorkerThread
import androidx.lifecycle.AndroidViewModel
import androidx.preference.PreferenceManager
import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.database.HighScoreDB
import de.saschahlusiak.freebloks.model.Game
import de.saschahlusiak.freebloks.model.GameMode
import de.saschahlusiak.freebloks.model.PlayerScore
import de.saschahlusiak.freebloks.network.message.MessageServerStatus
import de.saschahlusiak.freebloks.DependencyProvider
import de.saschahlusiak.freebloks.crashReporter
import kotlin.concurrent.thread

class GameFinishFragmentViewModel(app: Application) : AndroidViewModel(app) {
    val gameHelper = DependencyProvider.googlePlayGamesHelper()
    private var unlockAchievementsCalled = false

    // prefs
    val prefs: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(getApplication()) }

    // game data to display
    var game: Game? = null
    var lastStatus: MessageServerStatus? = null
    var data: Array<PlayerScore>? = null
    var localClientName: String? = null
    val gameMode get() = game?.gameMode

    // LiveData
    val isSignedIn = gameHelper.signedIn

    fun isInitialised() = (game != null)

    fun setDataFromBundle(bundle: Bundle) {
        if (this.game != null) throw IllegalStateException("Already initialised")

        val game = bundle.getSerializable("game") as? Game
        this.game = game ?: throw IllegalArgumentException("game must not be null")
        this.lastStatus = bundle.getSerializable("lastStatus") as MessageServerStatus?
        this.localClientName = prefs.getString("player_name", null)?.ifBlank { null }

        this.data = game.getPlayerScores().also { data ->
            // assign names to the scores based on lastStatus and clientName
            assignClientNames(game.gameMode, data, lastStatus)

            // the first time we set data and calculate it, we add it to the database
            thread { addScores(data, game.gameMode) }

            // and unlock achievements if we are logged in
            if (gameHelper.signedIn.value == true && !unlockAchievementsCalled) {
                thread { unlockAchievements(data, game.gameMode) }
            }
        }
    }

    private fun assignClientNames(gameMode: GameMode, scores: Array<PlayerScore>, lastStatus: MessageServerStatus?) {
        val context: Context = getApplication()

        scores.forEach { score ->
            val colorName = Global.getColorName(context, score.color1, gameMode)

            if (score.isLocal && localClientName != null) {
                score.clientName = localClientName
            } else {
                score.clientName = lastStatus?.getPlayerName(score.color1) ?: colorName
            }
        }
    }

    fun unlockAchievements() {
        val data = data ?: return
        val game = game?: return
        if (!unlockAchievementsCalled) {
            thread { unlockAchievements(data, game.gameMode) }
        }
    }

    @WorkerThread
    private fun addScores(scores: Array<PlayerScore>, gameMode: GameMode) {
        val db = HighScoreDB(getApplication())

        try {
            db.open()

            scores
                .filter { it.isLocal }
                .forEach { score ->
                    var flags = 0
                    if (score.isPerfect) flags = flags or HighScoreDB.FLAG_IS_PERFECT

                    db.addHighScore(gameMode, score.totalPoints, score.stonesLeft, score.color1, score.place, flags)
                }

            db.close()
        } catch (e: SQLiteException) {
            crashReporter.logException(e)
            e.printStackTrace()
        }
    }

    @WorkerThread
    private fun unlockAchievements(scores: Array<PlayerScore>, gameMode: GameMode) {
        // ensure we are only calling this once during the lifetime of the view model
        synchronized(this) {
            if (unlockAchievementsCalled) return
            unlockAchievementsCalled = true
        }

        val context: Context = getApplication()

        scores
            .filter { it.isLocal }
            .forEach { d ->
                if (gameMode == GameMode.GAMEMODE_4_COLORS_4_PLAYERS && d.place == 1)
                    gameHelper.unlock(context.getString(R.string.achievement_blokus_classic))

                if (gameMode == GameMode.GAMEMODE_4_COLORS_4_PLAYERS && d.isPerfect)
                    gameHelper.unlock(context.getString(R.string.achievement_perfect))

                if (gameMode == GameMode.GAMEMODE_DUO && d.place == 1)
                    gameHelper.unlock(context.getString(R.string.achievement_blokus_duo))

                gameHelper.increment(context.getString(R.string.achievement_1000_points), d.totalPoints)

                if (d.place == 1)
                    gameHelper.increment(context.getString(R.string.achievement_winner), 1)

                if (gameMode === GameMode.GAMEMODE_4_COLORS_4_PLAYERS && d.place == 4)
                    gameHelper.increment(context.getString(R.string.achievement_loser), 1)

                lastStatus?.let { lastStatus ->
                    if (lastStatus.clients >= 4 && d.place == 1)
                        gameHelper.unlock(context.getString(R.string.achievement_multiplayer))
                }
            }

        gameHelper.increment(context.getString(R.string.achievement_addicted), 1)

        val db = HighScoreDB(getApplication())
        try {
            db.open()
        } catch (e: SQLiteException) {
            crashReporter.logException(e)
            return
        }

        var n = 0
        for (i in 0..3) if (db.getNumberOfPlace(GameMode.GAMEMODE_4_COLORS_4_PLAYERS, 1, i) > 0) n++

        if (db.getNumberOfPlace(GameMode.GAMEMODE_DUO, 1, 0) > 0) n++
        if (db.getNumberOfPlace(GameMode.GAMEMODE_DUO, 1, 2) > 0) n++

        if (n == 6) gameHelper.unlock(context.getString(R.string.achievement_all_colors))

        gameHelper.submitScore(
            context.getString(R.string.leaderboard_games_won),
            db.getNumberOfPlace(null, 1).toLong()
        )

        gameHelper.submitScore(
            context.getString(R.string.leaderboard_points_total),
            db.getTotalNumberOfPoints(null).toLong()
        )

        db.close()
    }
}