package de.saschahlusiak.freebloks.network;

import java.io.ByteArrayOutputStream;

public class NET_REVOKE_PLAYER extends NET_HEADER {
	public int player; /* int 8 */

	public NET_REVOKE_PLAYER(int player) {
		super(MessageType.RevokePlayer, 1);
		this.player = player;
	}

	@Override
	void prepare(ByteArrayOutputStream bos) {
		super.prepare(bos);
		bos.write(player);
	}
}
