package de.saschahlusiak.freebloks.network;

import java.net.Socket;

import android.util.Log;

public class Network {
	static final String tag = Network.class.getSimpleName();

	public static final int DEFAULT_PORT = 59995;

	public static final int MSG_REQUEST_PLAYER = 1;
	public static final int MSG_GRANT_PLAYER = 2;
	public static final int MSG_CURRENT_PLAYER = 3;
	public static final int MSG_SET_STONE = 4;
	public static final int MSG_START_GAME = 5;
	public static final int MSG_GAME_FINISH = 6;
	public static final int MSG_SERVER_STATUS = 7;
	public static final int MSG_CHAT = 8;
	public static final int MSG_REQUEST_UNDO = 9;
	public static final int MSG_UNDO_STONE = 10;
	public static final int MSG_REQUEST_HINT = 11;
	public static final int MSG_STONE_HINT = 12;
	public static final int MSG_REVOKE_PLAYER = 13;
	public static final int MSG_REQUEST_GAME_MODE = 14;

	public static NET_HEADER read_package(Socket socket, boolean block) throws Exception {
		NET_HEADER p = new NET_HEADER(0, 0);
		if (! p.read(socket, block))
			return null;

//		Log.d(tag, "Received network package type " + p.msg_type);

		switch (p.msg_type) {
		case MSG_REQUEST_PLAYER:
			return new NET_REQUEST_PLAYER(p);
		case MSG_GRANT_PLAYER:
			return new NET_GRANT_PLAYER(p);
		case MSG_CURRENT_PLAYER:
			return new NET_CURRENT_PLAYER(p);
		case MSG_SET_STONE:
			return new NET_SET_STONE(p);
		case MSG_START_GAME:
			return new NET_START_GAME(p);
		case MSG_GAME_FINISH:
			return new NET_GAME_FINISH(p);
		case MSG_SERVER_STATUS:
			return new NET_SERVER_STATUS(p);
		case MSG_CHAT:
			return new NET_CHAT(p);
		case MSG_REQUEST_UNDO:
			return new NET_REQUEST_UNDO(p);
		case MSG_UNDO_STONE:
			return new NET_UNDO_STONE(p);
		case MSG_REQUEST_HINT:
			return new NET_REQUEST_HINT(p);
		case MSG_STONE_HINT:
			return new NET_SET_STONE(p);
		case MSG_REVOKE_PLAYER:
			return new NET_REVOKE_PLAYER(p);

		default:
			//throw new Exception("Unhandled message type " + p.msg_type);
			Log.e(tag, "Unhandled message type: " + p.msg_type);
			return null;
		}
	}
}
