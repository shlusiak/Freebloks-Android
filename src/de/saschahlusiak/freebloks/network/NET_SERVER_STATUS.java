package de.saschahlusiak.freebloks.network;

import java.io.ByteArrayOutputStream;
import de.saschahlusiak.freebloks.model.Stone;

public class NET_SERVER_STATUS extends NET_HEADER {
	public int player, computer, clients; /* int 8 */
	public int width, height; /* int 8 */
	public int stone_numbers[] = new int[Stone.STONE_SIZE_MAX]; /* int8[5] */
	public int gamemode; /* int8 */
	
	/* since 1.5, optional */
	public int spieler[]; /* int8[4] */
	public String client_names[]; /* uint8[8][16] */
	
	
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
		if (from.data_length >= 11 + 4 + 16 * 8) {
			/* advanced */
			spieler = new int[4];
			spieler[0] = buffer[11];
			spieler[1] = buffer[12];
			spieler[2] = buffer[13];
			spieler[3] = buffer[14];
			
			client_names = new String[8];
			byte tmp[] = new byte[16];
			for (int i = 0; i < 8; i++) {
				int j;
				for (j = 0; j < 16; j++) {
					tmp[j] = buffer[15 + i * 16 + j];
					if (tmp[j] == 0)
						break;
				}
				if (j > 0)
					client_names[i] = new String(tmp, 0, j);
				else
					client_names[i] = null;
			}
		}
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
	
	public boolean isAdvanced() {
		return (spieler != null);
	}
	
}
