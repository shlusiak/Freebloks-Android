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
import de.saschahlusiak.freebloks.utils.GooglePlayGamesHelper
import kotlinx.coroutines.Dispatchers
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

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    val gamesHelper: GooglePlayGamesHelper,
    prefs: Preferences,
    private val db: HighScoreDatabase
) : ViewModel() {
    internal val gameMode = MutableStateFlow(prefs.gameMode)

    val signedIn = gamesHelper.signedIn

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

    internal val data: StateFlow<List<RowData>> = gameMode
        .flatMapLatest { mode ->
            db.getAllAsFlow(mode)
                .map { list -> dataForMode(mode, list) }
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


    private fun dataForMode(gameMode: GameMode, entries: List<HighScoreEntry>): List<RowData> = buildList {
        val games = entries.size
        val points = entries.sumOf { it.points }
        val perfect = entries.count { it.isPerfect }
        val good = entries.count { it.stonesLeft == 0 } - perfect
        val stonesLeft = entries.sumOf { it.stonesLeft }

        val stonesUsed = games * Shape.COUNT - stonesLeft

        add(R.string.statistics_label_games_played to games.toString())

        if (games > 0) {
            add(
                R.string.statistics_label_good_games to String.format(
                    Locale.ROOT,
                    "%.1f%%",
                    100.0f * good.toFloat() / games.toFloat()
                )
            )

            add(
                R.string.statistics_label_perfect_games to String.format(
                    Locale.ROOT,
                    "%.1f%%",
                    100.0f * perfect.toFloat() / games.toFloat()
                )
            )

            var i = 0
            while (i < gameMode.colors) {
                val label = when (i) {
                    0 -> R.string.statistics_label_1st
                    1 -> R.string.statistics_label_2nd
                    2 -> R.string.statistics_label_3rd
                    else -> R.string.statistics_label_4th
                }
                val n = entries.count {
                    it.place == i + 1
                }
                add(
                    label to String.format(
                        Locale.ROOT,
                        "%.1f%%", 100.0f * n.toFloat() / games.toFloat()
                    )
                )
                i++
            }

            add(
                R.string.statistics_label_stones_used to String.format(
                    Locale.ROOT,
                    "%.1f%%",
                    100.0f * stonesUsed.toFloat() / games.toFloat() / Shape.COUNT.toFloat()
                )
            )
        }

        add(R.string.statistics_label_points_total to String.format("%d", points))
    }

    companion object {
        private val TAG = StatisticsViewModel::class.simpleName
    }
}