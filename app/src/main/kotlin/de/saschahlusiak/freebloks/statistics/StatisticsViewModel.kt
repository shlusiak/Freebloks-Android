package de.saschahlusiak.freebloks.statistics

import android.content.Context
import androidx.lifecycle.ViewModel
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

typealias RowData = Pair<Int, String>

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    val gamesHelper: GooglePlayGamesHelper,
    prefs: Preferences,
    private val db: HighScoreDB,
    @ApplicationContext private val context: Context
) : ViewModel() {
    internal val gameMode = MutableStateFlow(prefs.gameMode)

    val signedIn = gamesHelper.signedIn

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
                val games = db.getTotalNumberOfGames(gameMode)
                val points = db.getTotalNumberOfPoints(gameMode)
                val perfect = db.getNumberOfPerfectGames(gameMode)
                val good = db.getNumberOfGoodGames(gameMode) - perfect
                val stonesLeft = db.getTotalNumberOfStonesLeft(gameMode)

                val stonesUsed = games * Shape.COUNT - stonesLeft

                add(R.string.statistics_label_games_played to games.toString())

                if (games > 0) {
                    add(R.string.statistics_label_good_games to String.format("%.1f%%", 100.0f * good.toFloat() / games.toFloat()))

                    add(R.string.statistics_label_perfect_games to String.format("%.1f%%", 100.0f * perfect.toFloat() / games.toFloat()))

                    var i = 0
                    while (i < gameMode.colors) {
                        val label = when(i) {
                            0 -> R.string.statistics_label_1st
                            1 -> R.string.statistics_label_2nd
                            2 -> R.string.statistics_label_3rd
                            else -> R.string.statistics_label_4th
                        }
                        val n = db.getNumberOfPlace(gameMode, i + 1)
                        add(label to String.format("%.1f%%", 100.0f * n.toFloat() / games.toFloat()))
                        i++
                    }

                    add(
                        R.string.statistics_label_stones_used to String.format(
                            "%.1f%%",
                            100.0f * stonesUsed.toFloat() / games.toFloat() / Shape.COUNT.toFloat()
                        )
                    )
                }

                add(R.string.statistics_label_points_total to String.format("%d", points))
            }
        }
}