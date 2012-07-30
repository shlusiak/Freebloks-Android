package de.saschahlusiak.freebloks.game;

import android.util.Log;
import android.widget.Toast;
import de.saschahlusiak.freebloks.controller.SpielClient;
import de.saschahlusiak.freebloks.controller.SpielClientInterface;
import de.saschahlusiak.freebloks.model.Ki;
import de.saschahlusiak.freebloks.model.Player;
import de.saschahlusiak.freebloks.model.Spiel;
import de.saschahlusiak.freebloks.model.Stone;
import de.saschahlusiak.freebloks.model.Turn;
import de.saschahlusiak.freebloks.network.NET_CHAT;
import de.saschahlusiak.freebloks.network.NET_SERVER_STATUS;
import de.saschahlusiak.freebloks.network.NET_SET_STONE;
import de.saschahlusiak.freebloks.view.ViewInterface;


class SpielClientThread extends Thread implements SpielClientInterface {
	static final String tag = SpielClientThread.class.getSimpleName();
	SpielClient spiel;
	boolean godown;
	Ki ki = new Ki();
	ViewInterface view = null;
	FreebloksActivity activity = null;
	boolean request_player;
	
	SpielClientThread(SpielClient spiel, boolean request_player) {
		this.spiel = spiel;
		this.view = null;
		this.activity = null;
		this.request_player = request_player;
		spiel.addClientInterface(this);
	}
	
	public synchronized boolean getGoDown() {
		return godown;
	}
	
	public void setView(FreebloksActivity activity, ViewInterface view) {
		this.view = view;
		this.activity = activity;
	}
	
	public synchronized void goDown() {
		godown = true;
	}

	@Override
	public void run() {
		godown = false;

		if (request_player)
			spiel.request_player();
//		spiel.request_player();
//		spiel.request_player();
//		spiel.request_player();

		do {
			if (!spiel.poll(true))
				break;
			if (getGoDown()) {
				Log.i("KIThread", "detaching");
				spiel.removeClientInterface(this);
				return;
			}
		} while (spiel.isConnected());
		spiel.disconnect();
		spiel.removeClientInterface(this);
		Log.i("KIThread", "disconnected, thread going down");
	}
	
	public void gameStarted() {
		int i;
		Log.d(tag, "Game started");
		for (i = 0; i < Spiel.PLAYER_MAX; i++)
			if (spiel.is_local_player(i))
				Log.d(tag, "Local player: " + i);
		if (view != null)
			view.updateView();
	}

	public void newCurrentPlayer(int player) {
		updateStoneGallery(player);
		
		if (!spiel.is_local_player())
			return;

		/* Ermittle CTurn, den die KI jetzt setzen wuerde */
		if (view != null)
			view.updateView();
		
		if (1 == 1)
			return;
		
		Turn turn = ki.get_ki_turn(spiel, spiel.current_player(), 5);
		Stone stone;
		if (turn == null) {
			Log.e(tag, "Player " + player + ": Did not find a valid move");
			return;
		}
		stone = spiel.get_current_player().get_stone(turn.m_stone_number);
		stone.mirror_rotate_to(turn.m_mirror_count, turn.m_rotate_count);
		spiel.set_stone(stone, turn.m_stone_number, turn.m_y, turn.m_x);
	}
	
	void updateStoneGallery(int player) {
		final Player p = (player < 0) ? null : spiel.get_player(player);
	}
	
	public void chatReceived(final NET_CHAT c) {
		if (spiel.current_player() < 0)
			return;
		if (activity == null)
			return;
		
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (c.client == -1)
					Toast.makeText(activity, "* " + c.text,
							Toast.LENGTH_LONG).show();
				else
					Toast.makeText(activity,
							"Client " + c.client + ": " + c.text,
							Toast.LENGTH_LONG).show();
			}
		});
	}

	public void gameFinished() {
		int i;
		Log.i(tag, "-- Game finished! --");
		for (i = 0; i < Spiel.PLAYER_MAX; i++) {
			Player player = spiel.get_player(i);
			Log.i(tag, (spiel.is_local_player(i) ? "*" : " ") + "Player " + i
					+ " has " + player.m_stone_count + " stones left and "
					+ -player.m_stone_points_left + " points.");
		}
//		updateStoneGallery();

		spiel.disconnect();
		view.updateView();
	}

	@Override
	public void stoneWasSet(NET_SET_STONE s) {
		view.updateView();
//		updateStoneGallery();
	}

	@Override
	public void hintReceived(NET_SET_STONE s) {
		// TODO Auto-generated method stub

	}

	@Override
	public void stoneUndone(Stone s, Turn t) {
		view.updateView();
//		updateStoneGallery();
	}

	@Override
	public void serverStatus(NET_SERVER_STATUS status) {
		// TODO Auto-generated method stub
		
	}
}
