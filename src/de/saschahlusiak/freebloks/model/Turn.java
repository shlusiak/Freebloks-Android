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


	public Turn(Turn from) {
		copy(from);
	}

	public Turn(int player, int stone, int y, int x, int mirror, int rotate) {
		init(player, stone, y, x, mirror, rotate);
	}

	final void copy(Turn from) {
		m_playernumber = from.m_playernumber;
		m_stone_number = from.m_stone_number;
		m_mirror_count = from.m_mirror_count;
		m_rotate_count = from.m_rotate_count;
		m_y = from.m_y;
		m_x = from.m_x;
	}

	final void init(int player, int stone, int y, int x, int mirror, int rotate) {
		m_playernumber = player;
		m_stone_number = stone;
		m_mirror_count = mirror;
		m_rotate_count = rotate;
		m_y = y;
		m_x = x;
	}
}
