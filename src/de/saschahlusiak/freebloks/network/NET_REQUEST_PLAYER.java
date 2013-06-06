package de.saschahlusiak.freebloks.network;

import java.io.ByteArrayOutputStream;

public class NET_REQUEST_PLAYER extends NET_HEADER {
	int player; /* int8 */
	String name; /* uint8[16] */

	public NET_REQUEST_PLAYER(int player, String name) {
		super(Network.MSG_REQUEST_PLAYER, 17);
		this.player = player;
		this.name = name;
	}
	
	public NET_REQUEST_PLAYER(NET_HEADER from) {
		super(from);
		if (data_length >= 17) {
			player = buffer[0];
			name = new String(buffer, 1, 16);
		} else {
			name = null;
			player = -1;
		}
	}
	
	@Override
	void prepare(ByteArrayOutputStream bos) {		
		super.prepare(bos);
		bos.write(player);
		if (name == null) {
			for (int i = 0; i < 16; i++)
				bos.write(0);
		} else {
			int i;
			for (i = 0; i < 15 && i < name.length(); i++)
				bos.write((int)name.charAt(i));
			for (; i < 16; i++)
				bos.write(0);
		}
	}
}
