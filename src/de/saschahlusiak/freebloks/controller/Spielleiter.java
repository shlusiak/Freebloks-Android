package de.saschahlusiak.freebloks.controller;

import de.saschahlusiak.freebloks.model.Player;
import de.saschahlusiak.freebloks.model.Spiel;
import de.saschahlusiak.freebloks.model.Stone;
import de.saschahlusiak.freebloks.model.Turn;
import de.saschahlusiak.freebloks.model.Turnpool;

public class Spielleiter extends Spiel {
	static final int PLAYER_COMPUTER = -2;
	static final int PLAYER_LOCAL = -1;
	
	public static final int GAMEMODE_2_COLORS_2_PLAYERS = 0;
	public static final int GAMEMODE_4_COLORS_2_PLAYERS = 1;
	public static final int GAMEMODE_4_COLORS_4_PLAYERS = 2;
	
	int m_current_player;
	int spieler[] = new int[Spiel.PLAYER_MAX];
	int m_gamemode;
	
	Turnpool history;

	Spielleiter(int size_y, int size_x) {
		super(size_y, size_x);
		m_current_player=-1;
		m_gamemode = GAMEMODE_4_COLORS_4_PLAYERS;
		for (int i=0;i<PLAYER_MAX;i++)spieler[i]=PLAYER_COMPUTER;
		history=new Turnpool();
	}
	
	void set_noplayer() { 
		m_current_player=-1;
	}
	
	protected int current_player() {
		return m_current_player;
	}

	protected Player get_current_player() { 
		if (m_current_player == -1) 
			return null; 
		else 
			return get_player(m_current_player);
	}
	
	int get_gamemode() {
		return m_gamemode;
	}
	
	void addHistory(Turn turn)
	{
		history.add_turn(turn);
	}

	void addHistory(int player, Stone stone, int y, int x)
	{
		history.add_turn(player, stone, y, x);
	}

	int num_players()
	{
		int n;
		n=0;
		for (int i=0;i<PLAYER_MAX;i++)if (spieler[i]!=PLAYER_COMPUTER)n++;
		return n;
	}

	
}
