package de.saschahlusiak.freebloks.statistics

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.theme.AppTheme
import de.saschahlusiak.freebloks.model.GameMode

@AndroidEntryPoint
class StatisticsActivity : AppCompatActivity() {
    private val viewModel: StatisticsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val data by viewModel.data.collectAsState(null)
            val gamesData by viewModel.gamesData.collectAsState(null)
            val gameMode by viewModel.gameMode.collectAsState()

            AppTheme {
                StatisticsScreen(
                    data = data,
                    gamesData = gamesData,
                    gameMode = gameMode,
                    onBack = { finish() },
                    setGameMode = { viewModel.gameMode.value = it },
                    onReset = ::onResetStatistics
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun StatisticsScreen(
        data: StatisticsData?,
        gamesData: GooglePlayGamesData?,
        gameMode: GameMode,
        onBack: () -> Unit,
        setGameMode: (GameMode) -> Unit,
        onReset: () -> Unit
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(id = R.string.statistics)) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
                    },
                    actions = {
                        TextButton(onClick = onReset) {
                            Text(text = stringResource(id = R.string.prefs_clear_statistics))
                        }
                    }
                )
            }
        ) { padding ->
            StatisticsContent(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
                gameMode = gameMode,
                data = data,
                gamesData = gamesData,
                onGameMode = setGameMode,
                onSignIn = ::onSignIn,
                onSignOut = ::onSignOut,
                onLeaderboards = ::onLeaderboards,
                onAchievements = ::onAchievements
            )
        }
    }

    private fun onSignIn() {
        viewModel.gamesHelper.beginUserInitiatedSignIn(this@StatisticsActivity, REQUEST_SIGN_IN)
    }

    private fun onSignOut() {
        viewModel.gamesHelper.startSignOut()
    }

    private fun onAchievements() {
        viewModel.gamesHelper.startAchievementsIntent(this, REQUEST_ACHIEVEMENTS)
    }

    private fun onLeaderboards() {
        viewModel.gamesHelper.startLeaderboardIntent(
            this,
            getString(R.string.leaderboard_points_total),
            REQUEST_LEADERBOARD
        )
    }

    private fun onResetStatistics() {
        MaterialAlertDialogBuilder(this).apply {
            setMessage(R.string.clear_statistics_question)
            setPositiveButton(android.R.string.ok) { d, _ -> viewModel.resetStatistics() }
            setNegativeButton(android.R.string.cancel, null)
            show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_SIGN_IN -> viewModel.gamesHelper.onActivityResult(resultCode, data) { error ->
                MaterialAlertDialogBuilder(this).apply {
                    setMessage(error ?: getString(R.string.google_play_games_signin_failed))
                    setPositiveButton(android.R.string.ok) { d, _ -> d.dismiss() }
                    show()
                }
            }

            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object {
        private const val REQUEST_LEADERBOARD = 1
        private const val REQUEST_ACHIEVEMENTS = 2
        private const val REQUEST_SIGN_IN = 3
    }
}