package de.saschahlusiak.freebloks.network;

import java.io.ByteArrayOutputStream;

public class NET_SET_STONE extends NET_HEADER {
	public int player; /* int 8 */
	public int stone; /* uint8 */
	public int mirror_count, rotate_count; /* uint8 */
	public int x, y; /* int8 */
		
	public NET_SET_STONE() {
		this(Network.MSG_SET_STONE);
	}
	
	public NET_SET_STONE(int msg_type) {
		super(msg_type, 6);
	}
	
	public NET_SET_STONE(NET_HEADER from) {
		super(from);
		player = buffer[0];
		stone = buffer[1];
		mirror_count = buffer[2];
		rotate_count = buffer[3];
		x = buffer[4];
		y = buffer[5];
	}
	
	@Override
	void prepare(ByteArrayOutputStream bos) {
		super.prepare(bos);
		bos.write(player);
		bos.write(stone);
		bos.write(mirror_count);
		bos.write(rotate_count);
		bos.write(x);
		bos.write(y);
	}
}
