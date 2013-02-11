package de.saschahlusiak.freebloks.network;

import java.io.ByteArrayOutputStream;

public class NET_REQUEST_HINT extends NET_HEADER {
	public int player; /* int8 */
		
	public NET_REQUEST_HINT(int player) {
		super(Network.MSG_REQUEST_HINT, 1);
		this.player = player;
	}
	
	public NET_REQUEST_HINT(NET_HEADER from) {
		super(from);
		player = buffer[0];
	}
	
	@Override
	void prepare(ByteArrayOutputStream bos) {
		super.prepare(bos);
		bos.write(player);
	}
}
