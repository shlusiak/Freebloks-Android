package de.saschahlusiak.freebloks.network;

import java.io.ByteArrayOutputStream;

public class NET_CURRENT_PLAYER extends NET_HEADER {
	public int player; /* int 8 */

	public NET_CURRENT_PLAYER() {
		super(Network.MSG_CURRENT_PLAYER, 1);
	}

	public NET_CURRENT_PLAYER(NET_HEADER from) {
		super(from);
		player = buffer[0];
	}

	@Override
	void prepare(ByteArrayOutputStream bos) {
		super.prepare(bos);
		bos.write(player);
	}
}
