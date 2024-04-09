package de.saschahlusiak.freebloks.game.finish

import android.app.Application
import android.content.Context
import android.database.sqlite.SQLiteException
import androidx.annotation.WorkerThread
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.Preferences
import de.saschahlusiak.freebloks.database.HighScoreDB
import de.saschahlusiak.freebloks.model.Game
import de.saschahlusiak.freebloks.model.GameMode
import de.saschahlusiak.freebloks.model.PlayerScore
import de.saschahlusiak.freebloks.model.colorOf
import de.saschahlusiak.freebloks.network.message.MessageServerStatus
import de.saschahlusiak.freebloks.utils.CrashReporter
import de.saschahlusiak.freebloks.utils.GooglePlayGamesHelper
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.concurrent.thread

@HiltViewModel
class GameFinishFragmentViewModel @Inject constructor(
    args: SavedStateHandle,
    prefs: Preferences,
    db: HighScoreDB,
    private val app: Application,
    private val crashReporter: CrashReporter,
    private val gameHelper: GooglePlayGamesHelper
) : ViewModel() {
    private val db: Deferred<HighScoreDB> = viewModelScope.async(Dispatchers.IO) {
        db.apply { open() }
    }

    // game data to display
    val game: Game
    private var lastStatus: MessageServerStatus? = null
    val playerScores: List<PlayerScore>
    private var localClientName: String
    val gameMode get() = game.gameMode

    // LiveData
    val isSignedIn = gameHelper.signedIn

    init {
        val game = args.get("game") as? Game
        this.game = game ?: throw IllegalArgumentException("game must not be null")
        this.lastStatus = args.get("lastStatus") as MessageServerStatus?
        this.localClientName = prefs.playerName

        this.playerScores = game.getPlayerScores().also { data ->
            // assign names to the scores based on lastStatus and clientName
            assignClientNames(game.gameMode, data, lastStatus)
        }

        // the first time we set data and calculate it, we add it to the database
        viewModelScope.launch {
            addScores(playerScores, game.gameMode)

            // and unlock achievements if we are logged in
            if (gameHelper.signedIn.value == true) {
                unlockAchievements(playerScores, game.gameMode)
            }
        }
    }

    override fun onCleared() {
        GlobalScope.launch {
            try {
                db.await().close()
            } catch (e: Exception) {
                crashReporter.logException(e)
            }
        }
        super.onCleared()
    }

    private fun assignClientNames(gameMode: GameMode, scores: List<PlayerScore>, lastStatus: MessageServerStatus?) {
        val context: Context = app

        scores.forEach { score ->
            val colorName = gameMode.colorOf(score.color1).getName(context.resources)

            if (score.isLocal && localClientName.isNotBlank()) {
                score.clientName = localClientName
            } else {
                score.clientName = lastStatus?.getPlayerName(score.color1) ?: colorName
            }
        }
    }

    private suspend fun addScores(scores: List<PlayerScore>, gameMode: GameMode) {
        try {
            val db = db.await()

            scores
                .filter { it.isLocal }
                .forEach { score ->
                    var flags = 0
                    if (score.isPerfect) flags = flags or HighScoreDB.FLAG_IS_PERFECT

                    db.addHighScore(gameMode, score.totalPoints, score.stonesLeft, score.color1, score.place, flags)
                }
        } catch (e: SQLiteException) {
            crashReporter.logException(e)
            e.printStackTrace()
        }
    }

    private suspend fun unlockAchievements(scores: List<PlayerScore>, gameMode: GameMode) {
        val context: Context = app

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

        try {
            val db = db.await()

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
        } catch (e: SQLiteException) {
            crashReporter.logException(e)
        }
    }
}