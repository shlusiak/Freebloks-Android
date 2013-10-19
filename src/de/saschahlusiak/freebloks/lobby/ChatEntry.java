package de.saschahlusiak.freebloks.lobby;

import java.io.Serializable;

import de.saschahlusiak.freebloks.Global;

import android.graphics.Color;

public class ChatEntry implements Serializable {
	private static final long serialVersionUID = 1L;
	
	int client;
	String text, name;
	int player;
	
	public ChatEntry(int client, String text, String name) {
		this.client = client;
		this.text = text;
		this.name = name;
		
		player = -1;
	}
	
	public void setPlayer(int player) {
		this.player = player;
	}
	
	int getColor(int gamemode) {
		final int extra_colors[] = { Color.CYAN, Color.MAGENTA, Color.LTGRAY, Color.WHITE };

		if (player < 0)
			return extra_colors[client % extra_colors.length];
		else
			return Global.PLAYER_FOREGROUND_COLOR[Global.getPlayerColor(player, gamemode)];
	}
	
	@Override
	public String toString() {
		if (client < 0)
			return "* " + text;
		return name + ": " + text;
	}
}
