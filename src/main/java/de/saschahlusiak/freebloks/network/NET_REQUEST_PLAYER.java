package de.saschahlusiak.freebloks.network;

import java.io.ByteArrayOutputStream;

public class NET_REQUEST_PLAYER extends NET_HEADER {
	int player; /* int8 */
	String name; /* uint8[16] */

	public NET_REQUEST_PLAYER(int player, String name) {
		super(MessageType.RequestPlayer, 17);
		this.player = player;
		this.name = name;
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
			for (i = 0; i < 15 && i < name.length(); i++) {
				int c = (int)name.charAt(i);
				bos.write(c);
			}
			for (; i < 16; i++)
				bos.write(0);
		}
	}
}
