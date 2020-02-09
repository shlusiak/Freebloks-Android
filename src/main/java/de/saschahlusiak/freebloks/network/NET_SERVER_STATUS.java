package de.saschahlusiak.freebloks.network;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.security.InvalidParameterException;

import android.content.res.Resources;
import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.controller.GameMode;
import de.saschahlusiak.freebloks.model.Shape;

public class NET_SERVER_STATUS extends NET_HEADER implements Serializable {
	private static final long serialVersionUID = 1L;

	public int player, computer, clients; /* int 8 */
	public int width, height; /* int 8 */
	public int stone_numbers_obsolete[] = new int[Shape.SIZE_MAX]; /* int8[5] */
	public GameMode gamemode; /* int8 */

	/* since 1.5, optional */
	public int spieler[]; /* int8[4] */
	public String client_names[]; /* uint8[8][16] */

	public int version;
	public int version_min;
	
	public int stone_numbers[] = new int[Shape.COUNT];
	
	private static final int VERSION_MAX = 3; // highest version we understand.
	

	public NET_SERVER_STATUS(NET_HEADER from) throws ProtocolException {
		super(from);
		
		/* TODO: verify information, throw ProtocolException */
		player = buffer[0];
		computer = buffer[1];
		clients = buffer[2];
		width = buffer[3];
		height = buffer[4];
		gamemode = GameMode.from(buffer[10]);
		version = 1;
		version_min = 1;
		
		if (from.data_length >= 11 + 4 + 16 * 8)
			version = 2;
		
		if (from.data_length >= 11 + 4 + 16 * 8 + 2) {
			version = buffer[11 + 4 + 16 * 8 + 0];
			version_min = buffer[11 + 4 + 16 * 8 + 1];
		}
		
		if (version_min > VERSION_MAX) {
			/* we don't know how to speak version_min, the minimum required version */
			throw new ProtocolException("unsupported protocol version: " + version_min);
		}
		
		if (isVersion(2)) {
			/* advanced */
			spieler = new int[4];
			spieler[0] = buffer[11];
			spieler[1] = buffer[12];
			spieler[2] = buffer[13];
			spieler[3] = buffer[14];

			client_names = new String[8];
			char tmp[] = new char[16];
			for (int i = 0; i < 8; i++) {
				int j;
				for (j = 0; j < 16; j++) {
					tmp[j] = (char)unsigned(buffer[15 + i * 16 + j]);
					if (tmp[j] == 0)
						break;
				}
				if (j > 0)
					client_names[i] = new String(tmp, 0, j);
				else
					client_names[i] = null;
			}
		}
		for (int i = 0; i < Shape.SIZE_MAX; i++)
			stone_numbers_obsolete[i] = buffer[5 + i];
		if (isVersion(3)) {
			for (int i = 0; i < Shape.COUNT; i++)
				stone_numbers[i] = buffer[11 + 4 + 16 * 8 + 2 + i];
		}
	}
	
	public boolean isVersion(int version) {
		return this.version >= version;
	}

	@Override
	void prepare(ByteArrayOutputStream bos) {
		throw new RuntimeException("not implemented");
	}

	public String getClientName(Resources resources, int client) {
		if (client_names == null || client < 0 || client_names[client] == null)
			return resources.getString(R.string.client_d, client + 1);
		return client_names[client];
	}

	public String getPlayerName(Resources resources, int player, int color) {
		if (player < 0)
			throw new InvalidParameterException();

		String color_name = resources.getStringArray(R.array.color_names)[color];
		if (spieler == null)
			return color_name;
		if (client_names == null)
			return color_name;
		if (spieler[player] < 0)
			return color_name;
		if (client_names[spieler[player]] == null)
			return color_name;
		return client_names[spieler[player]];
	}
}
