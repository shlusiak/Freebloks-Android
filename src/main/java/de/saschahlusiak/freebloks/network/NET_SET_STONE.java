package de.saschahlusiak.freebloks.network;

import java.io.ByteArrayOutputStream;

import de.saschahlusiak.freebloks.model.Shape;
import de.saschahlusiak.freebloks.model.Turn;

public class NET_SET_STONE extends NET_HEADER {
	public int player; /* int 8 */
	public int stone; /* uint8 */
	public int mirror_count, rotate_count; /* uint8 */
	public int x, y; /* int8 */

	public NET_SET_STONE() {
		this(MessageType.SetStone);
	}

	public NET_SET_STONE(MessageType msg_type) {
		super(msg_type, 6);
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
