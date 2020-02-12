package de.saschahlusiak.freebloks.network;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.OutputStream;
import java.net.Socket;

public class SendHandler extends Handler {
	private final OutputStream os;

	public SendHandler(OutputStream os, Looper looper) {
		super(looper);
		this.os = os;
	}

	@Override
	public void handleMessage(Message msg) {
		if (msg.obj instanceof NET_HEADER) {
			final NET_HEADER m = (NET_HEADER) msg.obj;

			m.send(os);
		} else {
			throw new IllegalStateException("Not implemented");
		}
	}
}
