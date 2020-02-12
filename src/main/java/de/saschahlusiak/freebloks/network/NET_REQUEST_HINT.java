package de.saschahlusiak.freebloks.network;

import java.io.ByteArrayOutputStream;

public class NET_REQUEST_HINT extends NET_HEADER {
	public int player; /* int8 */

	public NET_REQUEST_HINT(int player) {
		super(MessageType.RequestHint, 1);
		this.player = player;
	}

	@Override
	void prepare(ByteArrayOutputStream bos) {
		super.prepare(bos);
		bos.write(player);
	}
}
