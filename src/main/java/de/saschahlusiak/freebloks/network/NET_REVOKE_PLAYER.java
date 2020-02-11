package de.saschahlusiak.freebloks.network;

import java.io.ByteArrayOutputStream;

public class NET_REVOKE_PLAYER extends NET_HEADER {
	public int player; /* int 8 */

	public NET_REVOKE_PLAYER(int player) {
		super(Network.MSG_REVOKE_PLAYER, 1);
		this.player = player;
	}

	public NET_REVOKE_PLAYER(NET_HEADER from) throws ProtocolException {
		super(from);
		player = data[0];
		
		if (player < 0 || player > 3)
			throw new ProtocolException("invalid player: " + player);
	}

	@Override
	void prepare(ByteArrayOutputStream bos) {
		super.prepare(bos);
		bos.write(player);
	}
}
