package de.saschahlusiak.freebloks.network;

import java.io.ByteArrayOutputStream;

public class NET_REQUEST_HINT extends NET_HEADER {
	public int player; /* int 8 */
		
	public NET_REQUEST_HINT() {
		super(Network.MSG_REQUEST_HINT, 1);
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
