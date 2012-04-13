package de.saschahlusiak.freebloks.network;

import java.io.ByteArrayOutputStream;

public class NET_GRANT_PLAYER extends NET_HEADER {
	public int player; /* int 8 */
		
	public NET_GRANT_PLAYER() {
		super(Network.MSG_GRANT_PLAYER, 1);
	}
	
	public NET_GRANT_PLAYER(NET_HEADER from) {
		super(from);
		player = buffer[0];
	}
	
	@Override
	void prepare(ByteArrayOutputStream bos) {
		super.prepare(bos);
		bos.write(player);
	}
}
