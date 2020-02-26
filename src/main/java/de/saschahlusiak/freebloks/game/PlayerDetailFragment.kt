package de.saschahlusiak.freebloks.game

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.model.Game

class PlayerDetailFragment : Fragment(R.layout.player_detail_fragment) {

    private val viewModel by lazy { ViewModelProvider(requireActivity()).get(FreebloksActivityViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.playerToShowInSheet.observe(this, Observer { updateViews(it) })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setOnApplyWindowInsetsListener { v: View, insets: WindowInsets ->
            v.setPadding(insets.systemWindowInsetLeft, 0, insets.systemWindowInsetRight, insets.systemWindowInsetBottom)
            insets
        }
    }

    private fun updateViews(showPlayer: Int) {
        val client = viewModel.client
        val statusView = view ?: return

        val status: TextView = statusView.findViewById(R.id.currentPlayer)
        val movesLeft: TextView = statusView.findViewById(R.id.movesLeft)
        val points: TextView = statusView.findViewById(R.id.points)
        val progressBar: View = statusView.findViewById(R.id.progressBar)

        progressBar.visibility = View.GONE
        points.visibility = View.GONE
        movesLeft.visibility = View.GONE

        status.clearAnimation()

        // the intro trumps everything
        if (viewModel.intro != null) {
            statusView.setBackgroundColor(Color.rgb(64, 64, 80))
            status.setText(R.string.touch_to_skip)
            return
        }

        // if not connected, show that
        if (client == null || !client.isConnected()) {
            statusView.setBackgroundColor(Color.rgb(64, 64, 80))
            status.setText(R.string.not_connected)
            return
        }

        val currentPlayer: Int = client.game.currentPlayer

        // no current player
        if (showPlayer < 0) {
            statusView.setBackgroundColor(Color.rgb(64, 64, 80))
            status.setText(R.string.no_player)
            return
        }

        val game: Game = client.game
        val board = game.board

        // is it "your turn"?
        val isYourTurn: Boolean = client.game.isLocalPlayer()

        val playerColor = Global.getPlayerColor(showPlayer, game.gameMode)
        val backgroundColorResource = Global.PLAYER_BACKGROUND_COLOR_RESOURCE[playerColor]
        // TODO: we can change the name in the settings when a game is running, which previously trumped this value
        // FIXME: also, on resume, we are not getting a server status with the names, apparently
        val playerName: String = client.lastStatus?.getPlayerName(showPlayer) ?: Global.getColorName(requireContext(), showPlayer, game.gameMode )
        val p = board.getPlayer(showPlayer)

        statusView.setBackgroundColor(resources.getColor(backgroundColorResource))

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
        if (showPlayer == currentPlayer) {
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