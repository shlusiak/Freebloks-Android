package de.saschahlusiak.freebloks.network;

import java.io.ByteArrayOutputStream;

import de.saschahlusiak.freebloks.model.Stone;

public class NET_SERVER_STATUS extends NET_HEADER {
	public int player, computer, clients; /* int 8 */
	public int width, height; /* int 8 */
	public int stone_numbers[] = new int[Stone.STONE_SIZE_MAX]; /* int8[5] */
	public int gamemode; /* int8 */
	
	public NET_SERVER_STATUS() {
		super(Network.MSG_SERVER_STATUS, 11);
	}
	
	public NET_SERVER_STATUS(NET_HEADER from) {
		super(from);
		player = buffer[0];
		computer = buffer[1];
		clients = buffer[2];
		width = buffer[3];
		height = buffer[4];
		for (int i = 0; i < Stone.STONE_SIZE_MAX; i++)
			stone_numbers[i] = buffer[5 + i];
		gamemode = buffer[10];
	}
	
	@Override
	void prepare(ByteArrayOutputStream bos) {
		super.prepare(bos);
		bos.write(player);
		bos.write(computer);
		bos.write(clients);
		bos.write(width);
		bos.write(height);
		for (int i = 0; i < Stone.STONE_SIZE_MAX; i++)
			bos.write(stone_numbers[i]);
		bos.write(gamemode);
	}
	
	
}
