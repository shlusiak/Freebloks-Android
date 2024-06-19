package de.saschahlusiak.freebloks.statistics

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import de.saschahlusiak.freebloks.R

@AndroidEntryPoint
class StatisticsActivity : AppCompatActivity() {
    private val viewModel: StatisticsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val data by viewModel.data.collectAsState(null)
            val gameMode by viewModel.gameMode.collectAsState()
            val signedIn by viewModel.signedIn.collectAsState(initial = null)

            StatisticsScreen(
                data = data,
                gameMode = gameMode,
                signedIn = signedIn,
                onBack = { finish() },
                onSignIn = ::onSignIn,
                onLeaderboards = ::onLeaderboards,
                onAchievements = ::onAchievements,
                setGameMode = { viewModel.gameMode.value = it },
                onReset = ::onResetStatistics
            )
        }
    }

    private fun onSignIn() {
        viewModel.gamesHelper.beginUserInitiatedSignIn(this@StatisticsActivity, REQUEST_SIGN_IN)
    }

    private fun onAchievements() {
        viewModel.gamesHelper.startAchievementsIntent(this, REQUEST_ACHIEVEMENTS)
    }

    private fun onResetStatistics() {
        MaterialAlertDialogBuilder(this).apply {
            setMessage(R.string.clear_statistics_question)
            setPositiveButton(android.R.string.ok) { d, _ -> viewModel.clear() }
            setNegativeButton(android.R.string.cancel, null)
            show()
        }
    }

    private fun onLeaderboards() {
        viewModel.gamesHelper.startLeaderboardIntent(
            this,
            getString(R.string.leaderboard_points_total),
            REQUEST_LEADERBOARD
        )
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