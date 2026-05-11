package de.saschahlusiak.freebloks.preferences

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.core.net.toUri
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import de.saschahlusiak.freebloks.BuildConfig
import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.about.AboutFragment
import de.saschahlusiak.freebloks.app.theme.AppTheme
import de.saschahlusiak.freebloks.statistics.StatisticsActivity
import de.saschahlusiak.freebloks.support.SupportFragment

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    private val viewModel: SettingsActivityViewModel by viewModels()

    private val REQUEST_GOOGLE_SIGN_IN = 1000
    private val REQUEST_GOOGLE_LEADERBOARD = 1001
    private val REQUEST_GOOGLE_ACHIEVEMENTS = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()

        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                Content()
            }
        }
    }

    @Composable
    private fun Content() {
        SettingsScreen(
            viewModel = viewModel,
            onBack = ::finish,
            onSignIn = ::onSignIn,
            onAchievements = ::onAchievements,
            onLeaderboard = ::onLeaderboard,
            onAbout = ::onAbout,
            onRate = ::onRate,
            onSupport = ::onSupport,
            onStatistics = ::onStatistics
        )
    }

    private fun onSignIn() {
        viewModel.googleHelper.beginUserInitiatedSignIn(
            activity = this,
            requestCode = REQUEST_GOOGLE_SIGN_IN
        )
    }

    private fun onAchievements() {
        viewModel.googleHelper.startAchievementsIntent(
            activity = this,
            requestCode = REQUEST_GOOGLE_ACHIEVEMENTS
        )
    }

    private fun onLeaderboard() {
        viewModel.googleHelper.startLeaderboardIntent(
            this,
            getString(R.string.leaderboard_points_total),
            REQUEST_GOOGLE_LEADERBOARD
        )
    }

    private fun onAbout() {
        AboutFragment().show(supportFragmentManager, null)
    }

    private fun onSupport() {
        SupportFragment().show(supportFragmentManager, null)
    }

    private fun onRate() {
        val uri = Global.getMarketURLString(BuildConfig.APPLICATION_ID).toUri()
        startActivity(Intent("android.intent.action.VIEW", uri))
    }

    private fun onStatistics() {
        val intent = Intent(this, StatisticsActivity::class.java)
        startActivity(intent)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_GOOGLE_SIGN_IN -> viewModel.googleHelper.onActivityResult(resultCode, data) { error ->
                MaterialAlertDialogBuilder(this).apply {
                    setMessage(error ?: getString(R.string.google_play_games_signin_failed))
                    setPositiveButton(android.R.string.ok) { d, _ -> d.dismiss() }
                    show()
                }
            }
        }
    }
}