package de.saschahlusiak.freebloks.statistics

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.Preferences
import de.saschahlusiak.freebloks.database.HighScoreDB
import de.saschahlusiak.freebloks.model.GameMode
import de.saschahlusiak.freebloks.model.Shape
import de.saschahlusiak.freebloks.utils.GooglePlayGamesHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import javax.inject.Inject

typealias RowData = Pair<String, String?>

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    val gamesHelper: GooglePlayGamesHelper,
    prefs: Preferences,
    private val db: HighScoreDB,
    @ApplicationContext private val context: Context
) : ViewModel() {
    internal val gameMode = MutableStateFlow(prefs.gameMode)

    val signedIn = gamesHelper.signedIn.asFlow()

    init {
        db.open()

        signedIn
            .onEach { signedIn ->
                if (signedIn) {
                    gamesHelper.submitScore(
                        context.getString(R.string.leaderboard_games_won),
                        db.getNumberOfPlace(null, 1).toLong()
                    )
                    gamesHelper.submitScore(
                        context.getString(R.string.leaderboard_points_total),
                        db.getTotalNumberOfPoints(null).toLong()
                    )
                }
            }
    }

    override fun onCleared() {
        db.close()
    }

    internal val data: StateFlow<List<RowData>> = gameMode
        .map { dataForMode(it) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private suspend fun dataForMode(gameMode: GameMode): List<RowData> =
        withContext(Dispatchers.IO) {
            buildList {
                val labels = context.resources.getStringArray(R.array.statistics_labels)

                val games = db.getTotalNumberOfGames(gameMode)
                val points = db.getTotalNumberOfPoints(gameMode)
                val perfect = db.getNumberOfPerfectGames(gameMode)
                val good = db.getNumberOfGoodGames(gameMode) - perfect
                val stonesLeft = db.getTotalNumberOfStonesLeft(gameMode)

                val stonesUsed = games * Shape.COUNT - stonesLeft

                add(labels[0] to String.format("%d", games))

                if (games > 0) {
                    add(labels[1] to String.format("%.1f%%", 100.0f * good.toFloat() / games.toFloat()))

                    add(labels[2] to String.format("%.1f%%", 100.0f * perfect.toFloat() / games.toFloat()))

                    var i = 0
                    while (i < gameMode.colors) {
                        val n = db.getNumberOfPlace(gameMode, i + 1)
                        add(labels[3 + i] to String.format("%.1f%%", 100.0f * n.toFloat() / games.toFloat()))
                        i++
                    }

                    add(
                        labels[7] to String.format(
                            "%.1f%%",
                            100.0f * stonesUsed.toFloat() / games.toFloat() / Shape.COUNT.toFloat()
                        )
                    )
                }


                add(labels[8] to String.format("%d", points))
            }
        }
}