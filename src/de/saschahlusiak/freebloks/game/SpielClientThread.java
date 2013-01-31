package de.saschahlusiak.freebloks.game;

import android.util.Log;
import de.saschahlusiak.freebloks.controller.SpielClient;


class SpielClientThread extends Thread {
	static final String tag = SpielClientThread.class.getSimpleName();
	SpielClient client;
	boolean godown;
	
	SpielClientThread(SpielClient spiel) {
		this.client = spiel;
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
			if (!client.poll(true))
				break;
			if (getGoDown()) {
				return;
			}
		} while (client.isConnected());
		client.disconnect();
		Log.i(tag, "disconnected, thread going down");
	}
}
