package de.saschahlusiak.freebloks.game.finish

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.theme.AppTheme
import de.saschahlusiak.freebloks.game.FreebloksActivityViewModel
import de.saschahlusiak.freebloks.game.OnStartCustomGameListener
import de.saschahlusiak.freebloks.game.lobby.LobbyDialog
import de.saschahlusiak.freebloks.statistics.StatisticsActivity
import de.saschahlusiak.freebloks.utils.AnalyticsProvider
import de.saschahlusiak.freebloks.utils.GooglePlayGamesHelper
import javax.inject.Inject

@AndroidEntryPoint
class GameFinishFragment : DialogFragment() {

    private val viewModel: GameFinishFragmentViewModel by viewModels()

    private val activityViewModel: FreebloksActivityViewModel by viewModels(ownerProducer = { requireActivity() })

    private val listener get() = requireActivity() as OnStartCustomGameListener

    override fun getTheme() = R.style.Theme_Freebloks_Dialog_MinWidth

    @Inject
    lateinit var analytics: AnalyticsProvider

    @Inject
    lateinit var gameHelper: GooglePlayGamesHelper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeView(requireContext())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view as ComposeView
        dialog?.window?.setBackgroundDrawable(null)
        dialog?.window?.let { gameHelper.setWindowForPopups(it) }

        view.setContent {
            AppTheme {
                Content()
            }
        }
    }

    @Composable
    private fun Content() {
        val gameMode = viewModel.gameMode
        val isSignedIn = viewModel.isSignedIn.collectAsState(initial = false)

        GameFinishScreen(
            gameMode = gameMode,
            data = viewModel.playerScores,
            isSignedIn = isSignedIn.value,
            onClose = { dismiss() },
            onNewGame = ::onNewGame,
            onAchievements = ::onAchievements,
            onLeaderboards = ::onLeaderboard,
            onStatistics = ::onStatistics,
            onChat = ::onChat.takeIf { viewModel.canChat }
        )
    }

    private fun onChat(message: String) {
        analytics.logEvent("finish_chat")

        activityViewModel.client?.sendChat(message)

        LobbyDialog().show(parentFragmentManager, null)
    }

    private fun onNewGame() {
        analytics.logEvent("finish_new_game_click")
        dismiss()
        if (activityViewModel.client?.config?.showLobby == true) {
            listener.restartGameWithLastConfiguration()
        } else {
            listener.showNewGameDialog()
        }
    }

    private fun onAchievements() {
        analytics.logEvent("finish_achievements_click")
        gameHelper.startAchievementsIntent(this, REQUEST_ACHIEVEMENTS)
    }

    private fun onLeaderboard() {
        analytics.logEvent("finish_leaderboard_click")
        gameHelper.startLeaderboardIntent(
            this,
            getString(R.string.leaderboard_points_total),
            REQUEST_LEADER_BOARD
        )
    }

    private fun onStatistics() {
        analytics.logEvent("finish_statistics_click")

        val intent = Intent(requireContext(), StatisticsActivity::class.java)
        startActivity(intent)
    }

    private fun onMainMenu() {
        analytics.logEvent("finish_main_menu_click")
        dismiss()
        listener.showMainMenu()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext(), theme).apply {
            setCanceledOnTouchOutside(false)
        }
    }

    companion object {
        private const val REQUEST_ACHIEVEMENTS = 1000
        private const val REQUEST_LEADER_BOARD = 1001
    }
}