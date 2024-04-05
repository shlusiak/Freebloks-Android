package de.saschahlusiak.freebloks.statistics

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.AppTheme

@AndroidEntryPoint
class StatisticsActivity : AppCompatActivity() {
    private val viewModel: StatisticsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Content()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun Content() {
        val data by viewModel.data.collectAsState()
        val gameMode by viewModel.gameMode.collectAsState()
        val signedIn by viewModel.signedIn.collectAsState(initial = null)

        AppTheme {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(stringResource(id = R.string.statistics)) },
                        navigationIcon = {
                            IconButton(onClick = { finish() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                            }
                        },
                        actions = {
                            if (signedIn == true) {
                                IconButton(
                                    onClick = ::onAchievements,
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_play_games_badge_achievements_white),
                                        contentDescription = null,
                                    )
                                }

                                IconButton(
                                    onClick = ::onLeaderboards,
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_play_games_badge_leaderboards_white),
                                        contentDescription = null
                                    )
                                }
                            }

                            if (signedIn == false) {
                                TextButton(onClick = { onSignIn() }) {
                                    Text(text = stringResource(id = R.string.google_play_games_signin))
                                }
                            }
                        }
                    )
                }
            ) { padding ->
                StatisticsContent(
                    modifier = Modifier.padding(padding),
                    gameMode = gameMode,
                    data = data
                ) { viewModel.gameMode.value = it }
            }
        }
    }

    private fun onSignIn() {
        viewModel.gamesHelper.beginUserInitiatedSignIn(this@StatisticsActivity, REQUEST_SIGN_IN)
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