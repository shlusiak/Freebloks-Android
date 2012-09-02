package de.saschahlusiak.freebloks.controller;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import android.util.Log;

import de.saschahlusiak.freebloks.model.Spiel;

public class ServerListener extends Thread {
	private static final String tag = ServerListener.class.getSimpleName();
	
	ServerSocket socket;
	SpielServer server;
	private boolean go_down;
	
	public ServerListener(InetAddress addr, int port, int ki_mode) {
		try {
			socket = new ServerSocket();
			socket.setReuseAddress(true);
			socket.bind(new InetSocketAddress(addr, port));
		} catch (Exception e) {
			e.printStackTrace();
		}
		go_down = false;
		server = new SpielServer(Spiel.DEFAULT_FIELD_SIZE_Y, Spiel.DEFAULT_FIELD_SIZE_X, ki_mode);
		server.listener = this;
	}
	
	public void close() {
		try {
			if (socket != null)
				socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		server = null;
		socket = null;
	}
	
	public void go_down() {
		go_down = true;
		close();
	}

	boolean wait_for_player() {
		Socket client;
		try {
			client = socket.accept();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		if (client == null)
			return false;
		
		/* Fuege den neuen Socket dem CSpielServer als Client hinzu */
		server.add_client(client);
		return true;
	}
	
	@Override
	public void run() {
		Log.d(tag, "starting up");
		do {
			if (!wait_for_player())
				break;
			if (interrupted())
				break;
			if (go_down)
				break;
		} while (true);
		Log.d(tag, "going down");
		close();
	}
}
