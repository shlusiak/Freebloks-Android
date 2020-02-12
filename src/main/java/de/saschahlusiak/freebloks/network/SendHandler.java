package de.saschahlusiak.freebloks.network;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.OutputStream;

public class SendHandler extends Handler {
	private final OutputStream os;
	private final MessageWriter writer;

	public SendHandler(OutputStream os, Looper looper) {
		super(looper);
		this.os = os;
		writer = new MessageWriter();
	}

	@Override
	public void handleMessage(Message msg) {
		if (msg.obj instanceof NET_HEADER) {
			final NET_HEADER m = (NET_HEADER) msg.obj;

			m.send(os);
		} else {
			final de.saschahlusiak.freebloks.network.Message m = (de.saschahlusiak.freebloks.network.Message)msg.obj;
			writer.write(os, m);
		}
	}
}
