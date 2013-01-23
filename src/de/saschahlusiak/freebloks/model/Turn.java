package de.saschahlusiak.freebloks.model;

import java.io.Serializable;

public class Turn implements Serializable {
	private static final long serialVersionUID = -1715006791524885742L;

	public int m_playernumber;

	public int m_stone_number;
	
	public int m_mirror_count;
	public int m_rotate_count;

	public int m_y;
	public int m_x;

	
	Turn(Turn from) {
		copy(from);
	}
	
	Turn(int player, Stone stone, int y, int x) {
		init(player, stone, y, x);
	}
	
	final void copy(Turn from) {
		m_playernumber = from.m_playernumber;
		m_stone_number = from.m_stone_number;
		m_mirror_count = from.m_mirror_count;
		m_rotate_count = from.m_rotate_count;
		m_y = from.m_y;
		m_x = from.m_x;
	}
	
	final void init(int player, Stone stone, int y, int x) {
		m_playernumber = player;
		m_stone_number = stone.get_number();
		m_mirror_count = stone.get_mirror_counter();
		m_rotate_count = stone.get_rotate_counter();
		m_y = y;
		m_x = x;
	}
}
