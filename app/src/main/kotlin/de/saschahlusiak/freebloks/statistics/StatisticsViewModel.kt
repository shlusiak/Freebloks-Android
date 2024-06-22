package de.saschahlusiak.freebloks.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.Preferences
import de.saschahlusiak.freebloks.database.HighScoreDatabase
import de.saschahlusiak.freebloks.database.entity.HighScoreEntry
import de.saschahlusiak.freebloks.model.GameMode
import de.saschahlusiak.freebloks.model.StoneColor
import de.saschahlusiak.freebloks.model.colorOf
import de.saschahlusiak.freebloks.utils.AnalyticsProvider
import de.saschahlusiak.freebloks.utils.GooglePlayGamesHelper
import de.saschahlusiak.freebloks.utils.LeaderboardEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
    val placesByColor: List<Pair<StoneColor, List<Int>>>
) {
    val perfectGamesPercent = (perfectGames * 100) / totalGames.coerceAtLeast(1)
    val goodGamesPercent = (goodGames * 100) / totalGames.coerceAtLeast(1)
}

data class GooglePlayGamesData(
    val isAvailable: Boolean,
    val isSignedIn: Boolean,
    val leaderboardData: List<LeaderboardEntry>
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    val gamesHelper: GooglePlayGamesHelper,
    prefs: Preferences,
    private val analytics: AnalyticsProvider,
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

    val gamesData = gamesHelper.signedIn
        .map {
            GooglePlayGamesData(
                isAvailable = gamesHelper.isAvailable,
                isSignedIn = it,
                leaderboardData = gamesHelper.getLeaderboard()
            )
        }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun resetStatistics() {
        viewModelScope.launch {
            analytics.logEvent("clear_statistics")
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

        val placesByColor = entries.groupBy { it.playerColor }
            .map { (player, list) ->
                val places = (1..gameMode.colors).map { place ->
                    list.count { it.place == place }
                }
                gameMode.colorOf(player) to places
            }.sortedByDescending { 100f * it.second[0] / it.second.sum().coerceAtLeast(1) }

        val places = (1..gameMode.colors).map { place ->
            entries.count { it.place == place }
        }

        return StatisticsData(
            gameMode = gameMode,
            totalGames = games,
            totalPoints = points,
            places = places,
            perfectGames = perfect,
            goodGames = good,
            placesByColor = placesByColor
        )
    }

    companion object {
        private val TAG = StatisticsViewModel::class.simpleName
    }
}