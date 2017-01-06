package de.saschahlusiak.freebloks.network;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.net.Socket;

public class SendHandler extends Handler {
	Socket socket;

	public SendHandler(Socket socket, Looper looper) {
		super(looper);
		this.socket = socket;
	}

	@Override
	public void handleMessage(Message msg) {
		NET_HEADER m = (NET_HEADER)msg.obj;
		m.send(socket);
	}
}
