package de.saschahlusiak.freebloks.game;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import de.saschahlusiak.freebloks.controller.SpielClient;


class SpielClientThread extends Thread {
	static final String tag = SpielClientThread.class.getSimpleName();
	SpielClient client;
	boolean godown;
	SendThread sendThread;
	
	static private class SendThread extends Thread {
		Looper looper;
		Handler handler;
		public void run()  {
			Looper.prepare();
			handler = new Handler();
			looper = Looper.myLooper();
			Looper.loop();
			Log.d("SendThread", "going down");
		}
	}
	
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
		sendThread = new SendThread();
		sendThread.start();
		do {
			if (!client.poll(true))
				break;
			if (getGoDown()) {
				return;
			}
		} while (client.isConnected());
		client.disconnect();
		sendThread.looper.quit();
		Log.i(tag, "disconnected, thread going down");
	}
	
	public void post(Runnable r) {
		if (sendThread == null)
			return;
		
		sendThread.handler.post(r);
	}
}
