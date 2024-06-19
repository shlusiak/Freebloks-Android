package de.saschahlusiak.freebloks.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.Preferences
import de.saschahlusiak.freebloks.database.HighScoreDatabase
import de.saschahlusiak.freebloks.database.entity.HighScoreEntry
import de.saschahlusiak.freebloks.model.GameMode
import de.saschahlusiak.freebloks.model.Shape
import de.saschahlusiak.freebloks.model.StoneColor
import de.saschahlusiak.freebloks.model.colorOf
import de.saschahlusiak.freebloks.model.stoneColors
import de.saschahlusiak.freebloks.utils.GooglePlayGamesHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

typealias RowData = Pair<Int, String>

typealias Percent = Int

data class StatisticsData(
    val gameMode: GameMode,
    val totalGames: Int,
    val totalPoints: Int,
    val places: List<Int>,
    val perfectGames: Int,
    val goodGames: Int,
    val winsByColor: List<Pair<StoneColor, Percent>>
) {
    val perfectGamesPercent = (perfectGames * 100) / totalGames.coerceAtLeast(1)
    val goodGamesPercent = (goodGames * 100) / totalGames.coerceAtLeast(1)
}

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    val gamesHelper: GooglePlayGamesHelper,
    prefs: Preferences,
    private val db: HighScoreDatabase
) : ViewModel() {
    internal val gameMode = MutableStateFlow(prefs.gameMode)

    internal val signedIn = gamesHelper.signedIn

    init {
        signedIn
            .onEach { signedIn ->
                if (signedIn) {
                    gamesHelper.submitScore(
                        R.string.leaderboard_games_won,
                        db.getNumberOfPlace(null, 1, null).toLong()
                    )
                    gamesHelper.submitScore(
                        R.string.leaderboard_points_total,
                        db.getTotalNumberOfPoints(null).toLong()
                    )
                }
            }
    }

    fun clear() {
        viewModelScope.launch {
            db.clear()
        }
    }

    internal val data: Flow<StatisticsData> = gameMode
        .flatMapLatest { mode ->
            db.getAllAsFlow(mode)
                .map { list -> dataForMode(mode, list) }
        }
        .flowOn(Dispatchers.Default)

    private fun dataForMode(gameMode: GameMode, entries: List<HighScoreEntry>): StatisticsData {
        val games = entries.size
        val points = entries.sumOf { it.points }
        val perfect = entries.count { it.isPerfect }
        val good = entries.count { it.stonesLeft == 0 } - perfect
        val stonesLeft = entries.sumOf { it.stonesLeft }

        val stonesUsed = games * Shape.COUNT - stonesLeft

        val places = (1..gameMode.colors).map { place ->
            entries.count { it.place == place }
        }

        val winsByColor = entries.groupBy { it.playerColor }
            .map { (player, list) ->
                val wins = list.count { it.place == 1 }
                gameMode.colorOf(player) to (wins * 100 / list.size.coerceAtLeast(1))
            }.sortedByDescending { it.second }

        return StatisticsData(
            gameMode = gameMode,
            totalGames = games,
            totalPoints = points,
            places = places,
            perfectGames = perfect,
            goodGames = good,
            winsByColor = winsByColor
        )
    }

    companion object {
        private val TAG = StatisticsViewModel::class.simpleName
    }
}