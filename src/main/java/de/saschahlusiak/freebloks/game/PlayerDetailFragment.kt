package de.saschahlusiak.freebloks.game

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.model.Game

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val cardView = view as CardView
        view.setOnApplyWindowInsetsListener { v: View, insets: WindowInsets ->
            cardView.setContentPadding(insets.systemWindowInsetLeft, 0, insets.systemWindowInsetRight, insets.systemWindowInsetBottom)
            insets
        }

        viewModel.playerToShowInSheet.observe(viewLifecycleOwner, Observer { updateViews(it) })
    }

    private fun updateViews(data: SheetPlayer) {
        val client = viewModel.client
        val background = view as? CardView ?: return

        val status: TextView = background.findViewById(R.id.currentPlayer)
        val movesLeft: TextView = background.findViewById(R.id.movesLeft)
        val points: TextView = background.findViewById(R.id.points)
        val progressBar: View = background.findViewById(R.id.progressBar)

        progressBar.visibility = View.GONE
        points.visibility = View.GONE
        movesLeft.visibility = View.GONE

        status.clearAnimation()

        // the intro trumps everything
        if (viewModel.intro != null) {
            background.setCardBackgroundColor(Color.rgb(64, 64, 80))
            status.setText(R.string.touch_to_skip)
            return
        }

        // if not connected, show that
        if (client == null || !client.isConnected()) {
            background.setCardBackgroundColor(Color.rgb(64, 64, 80))
            status.setText(R.string.not_connected)
            return
        }

        // no current player
        if (data.player < 0) {
            background.setCardBackgroundColor(Color.rgb(64, 64, 80))
            status.setText(R.string.no_player)
            return
        }

        val game: Game = client.game
        val board = game.board

        // is it "your turn", like, in general?
        val isYourTurn: Boolean = client.game.isLocalPlayer()

        val playerColor = Global.getPlayerColor(data.player, game.gameMode)
        val backgroundColorResource = Global.PLAYER_BACKGROUND_COLOR_RESOURCE[playerColor]
        // TODO: we can change the name in the settings when a game is running, which previously trumped this value
        // FIXME: also, on resume, we are not getting a server status with the names, apparently


        val playerName: String = client.lastStatus?.getPlayerName(data.player) ?: Global.getColorName(requireContext(), data.player, game.gameMode )
        val p = board.getPlayer(data.player)

        background.setCardBackgroundColor(resources.getColor(backgroundColorResource))

        points.visibility = View.VISIBLE
        points.text = resources.getQuantityString(R.plurals.number_of_points, p.totalPoints, p.totalPoints)

        if (client.game.isFinished) {
            status.text = "[$playerName]"
            movesLeft.visibility = View.VISIBLE
            movesLeft.text = resources.getQuantityString(R.plurals.number_of_stones_left, p.stonesLeft, p.stonesLeft)
            return
        }

        if (isYourTurn) {
            movesLeft.text = resources.getQuantityString(R.plurals.player_status_moves, p.numberOfPossibleTurns, p.numberOfPossibleTurns)
            movesLeft.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.VISIBLE
        }

        // we are showing "home"
        if (!data.isRotated) {
            if (isYourTurn) {
                status.text = getString(R.string.your_turn, playerName)
            } else {
                status.text = getString(R.string.waiting_for_color, playerName)
            }
        } else {
            if (p.numberOfPossibleTurns <= 0) status.text = "[" + getString(R.string.color_is_out_of_moves, playerName) + "]" else {
                status.text = playerName
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