package de.saschahlusiak.freebloks.network;

import java.io.ByteArrayOutputStream;

import de.saschahlusiak.freebloks.model.Stone;

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

	public NET_SET_STONE(NET_HEADER from) throws ProtocolException {
		super(from);
		player = buffer[0];
		stone = buffer[1];
		mirror_count = buffer[2];
		rotate_count = buffer[3];
		x = buffer[4];
		y = buffer[5];
		
		/* BUG: MSG_UNDO_STONE is actually a NET_SET_STONE with random payload;
		 * don't verify
		 */
		if (msg_type != Network.MSG_UNDO_STONE) {
			if (player < 0 || player > 3)
				throw new ProtocolException("invalid player: " + player);
			
			if (stone < 0 || stone >= Stone.STONE_COUNT_ALL_SHAPES)
				throw new ProtocolException("invalid stone: " + stone);
			
			if (mirror_count < 0 || mirror_count > 1)
				throw new ProtocolException("invalid mirror_count " + mirror_count);
			
			if (rotate_count < 0 || rotate_count > 3)
				throw new ProtocolException("invalid rotate_count " + rotate_count);
		}
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
