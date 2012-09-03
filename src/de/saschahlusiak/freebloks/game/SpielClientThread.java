package de.saschahlusiak.freebloks.game;

import android.util.Log;
import de.saschahlusiak.freebloks.controller.SpielClient;


class SpielClientThread extends Thread {
	static final String tag = SpielClientThread.class.getSimpleName();
	SpielClient spiel;
	boolean godown;
	boolean request_player;
	
	SpielClientThread(SpielClient spiel, boolean request_player) {
		this.spiel = spiel;
		this.request_player = request_player;
	}
	
	private synchronized boolean getGoDown() {
		return godown;
	}
	
	public synchronized void goDown() {
		godown = true;
	}

	@Override
	public void run() {
		godown = false;

		if (request_player)
			spiel.request_player();

		do {
			if (!spiel.poll(true))
				break;
			if (getGoDown()) {
				return;
			}
		} while (spiel.isConnected());
		spiel.disconnect();
		Log.i(tag, "disconnected, thread going down");
	}
}
