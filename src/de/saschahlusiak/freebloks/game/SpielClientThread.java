package de.saschahlusiak.freebloks.game;

import android.util.Log;
import de.saschahlusiak.freebloks.controller.SpielClient;


class SpielClientThread extends Thread {
	static final String tag = SpielClientThread.class.getSimpleName();
	SpielClient spiel;
	boolean godown;
	
	SpielClientThread(SpielClient spiel) {
		this.spiel = spiel;
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
