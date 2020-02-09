package de.saschahlusiak.freebloks.network;

import java.io.ByteArrayOutputStream;

import de.saschahlusiak.freebloks.controller.GameMode;
import de.saschahlusiak.freebloks.model.Stone;
import de.saschahlusiak.freebloks.model.StoneType;

public class NET_REQUEST_GAME_MODE extends NET_HEADER {
	private int version; /* int8 */
	int width, height; /* int8 */
	GameMode gamemode; /* int8 */
	int stone_numbers[]; /* int8[21] */

	private static final int NET_REQUEST_GAME_MODE_VERSION = 1;


	public NET_REQUEST_GAME_MODE(int width, int height, GameMode gamemode, int stone_numbers[]) {
		super(Network.MSG_REQUEST_GAME_MODE, 25);
		this.version = NET_REQUEST_GAME_MODE_VERSION;
		this.width = width;
		this.height = height;
		this.stone_numbers = stone_numbers;
		this.gamemode = gamemode;
	}

	@Override
	void prepare(ByteArrayOutputStream bos) {
		super.prepare(bos);
		bos.write(version);
		bos.write(width);
		bos.write(height);
		bos.write(gamemode.ordinal());
		for (int i = 0; i < StoneType.COUNT; i++)
			bos.write(stone_numbers[i]);
	}
}
