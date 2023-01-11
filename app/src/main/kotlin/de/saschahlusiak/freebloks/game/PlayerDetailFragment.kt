package de.saschahlusiak.freebloks.game

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.databinding.PlayerDetailFragmentBinding
import de.saschahlusiak.freebloks.model.Game
import de.saschahlusiak.freebloks.model.colorOf
import de.saschahlusiak.freebloks.utils.viewBinding

/**
 * The current player sheet at the bottom of the screen.
 *
 * Observes [FreebloksActivityViewModel.playerToShowInSheet]. There are several cases:
 *
 * - Not connected
 *   "Not connected"
 *
 * - Game in not started
 *   "No player"
 *
 * - Game is running
 *   If the board is not rotated, the current player.
 *   If the board is rotated, the rotated player.
 *   If the current player is a local player, show number of turns.
 *   If current player is remote player, show spinner.
 *
 * - Game is finished
 *   The board is rotating. At any time the status is showing the currently shown player and their left stones.
 *
 */
class PlayerDetailFragment : Fragment(R.layout.player_detail_fragment) {

    private val viewModel by lazy { ViewModelProvider(requireActivity()).get(FreebloksActivityViewModel::class.java) }

    private val binding by viewBinding(PlayerDetailFragmentBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val cardView = view as CardView
        view.setOnApplyWindowInsetsListener { _: View, insets: WindowInsets ->
            cardView.setContentPadding(insets.systemWindowInsetLeft, 0, insets.systemWindowInsetRight, insets.systemWindowInsetBottom)
            insets
        }

        viewModel.playerToShowInSheet.observe(viewLifecycleOwner) { updateViews(it) }
        viewModel.inProgress.observe(viewLifecycleOwner) { inProgressChanged(it) }
    }

    private fun inProgressChanged(inProgress: Boolean) {
        binding.movesLeft.isVisible = !inProgress
        binding.progressBar.isVisible = inProgress
    }

    private fun updateViews(data: SheetPlayer) = with(binding) {
        val client = viewModel.client
        val background = root

        currentPlayer.clearAnimation()

        // the intro trumps everything
        if (viewModel.intro != null) {
            background.setCardBackgroundColor(Color.rgb(64, 64, 80))
            currentPlayer.setText(R.string.touch_to_skip)
            points.visibility = View.GONE
            movesLeft.text = ""
            return
        }

        // if not connected, show that
        if (client == null || !client.isConnected()) {
            background.setCardBackgroundColor(Color.rgb(64, 64, 80))
            currentPlayer.setText(R.string.not_connected)
            points.visibility = View.GONE
            movesLeft.text = ""
            return
        }

        // no current player
        if (data.player < 0) {
            background.setCardBackgroundColor(Color.rgb(64, 64, 80))
            currentPlayer.setText(R.string.no_player)
            points.visibility = View.GONE
            movesLeft.text = ""
            return
        }

        val game: Game = client.game
        val board = game.board

        // is it "your turn", like, in general?
        val isYourTurn = client.game.isLocalPlayer()

        val playerColor = game.gameMode.colorOf(data.player)

        val playerName = viewModel.getPlayerName(data.player)
        val p = board.getPlayer(data.player)

        background.setCardBackgroundColor(resources.getColor(playerColor.backgroundColorId))

        points.visibility = View.VISIBLE
        points.text = resources.getQuantityString(R.plurals.number_of_points, p.totalPoints, p.totalPoints)

        if (client.game.isFinished) {
            currentPlayer.text = "[$playerName]"
            movesLeft.text = resources.getQuantityString(R.plurals.number_of_stones_left, p.stonesLeft, p.stonesLeft)
            return
        }

        movesLeft.text = resources.getQuantityString(R.plurals.player_status_moves, p.numberOfPossibleTurns, p.numberOfPossibleTurns)

        // we are showing "home"
        if (!data.isRotated) {
            if (isYourTurn) {
                currentPlayer.text = getString(R.string.your_turn, playerName)
            } else {
                currentPlayer.text = getString(R.string.waiting_for_color, playerName)
            }
        } else {
            if (p.numberOfPossibleTurns <= 0) currentPlayer.text = "[${getString(R.string.color_is_out_of_moves, playerName)}]" else {
                currentPlayer.text = playerName
            }
        }
    }

    private fun startAnimation() {
        // FIXME: animation of current player
/*
		final Animation a = new TranslateAnimation(0, 8, 0, 0);
		a.setInterpolator(new CycleInterpolator(2));
		a.setDuration(500);
		Runnable r = new Runnable() {
			@Override
			public void run() {
				if (view == null)
					return;
				boolean local = false;
				View t = findViewById(R.id.currentPlayer);
				t.postDelayed(this, 5000);

				if (client != null && client.game != null)
					local = client.game.isLocalPlayer();
				if (!local)
					return;

				t.startAnimation(a);
			}
		};
		findViewById(R.id.currentPlayer).postDelayed(r, 1000);
		*/
    }
}