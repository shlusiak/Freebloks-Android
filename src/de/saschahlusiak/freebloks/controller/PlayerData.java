package de.saschahlusiak.freebloks.controller;

import de.saschahlusiak.freebloks.model.Player;

public class PlayerData implements Comparable<PlayerData> {
	public int player1, player2;
	public int place;
	public int points, stones_left;
	public int bonus;
	public boolean is_local, is_perfect;
	
	PlayerData(Spielleiter spiel, int player) {
		this.place = -1;
		this.player1 = player;
		this.player2 = -1;
		this.is_local = spiel.is_local_player(player);
		this.is_perfect = true;
		addPoints(spiel.get_player(player));
	}
	
	PlayerData(Spielleiter spiel, int player1, int player2) {
		this.place = -1;
		this.player1 = player1;
		this.player2 = player2;
		this.is_local = spiel.is_local_player(player1);
		this.is_perfect = true;
		
		addPoints(spiel.get_player(player1));
		addPoints(spiel.get_player(player2));
	}
	
	void addPoints(Player p) {
		this.points += p.m_stone_points;
		this.stones_left += p.m_stone_count;
		if (p.m_stone_count == 0 && p.m_lastStone != null) {
			if (p.m_lastStone.get_stone_shape() == 0) {
				bonus += 20;
			}
			else {
				bonus += 15;
				is_perfect = false;
			}
		} else
			is_perfect = false;
	}

	@Override
	public int compareTo(PlayerData another) {
		if (points > another.points)
			return -1;
		if (points < another.points)
			return 1;
		if (stones_left < another.stones_left)
			return -1;
		if (stones_left > another.stones_left)
			return 1;
		return 0;
	}
}