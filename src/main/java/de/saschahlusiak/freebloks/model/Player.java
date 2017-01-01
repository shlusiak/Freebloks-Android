package de.saschahlusiak.freebloks.model;

import java.io.Serializable;

public class Player implements Serializable, Cloneable {
	private static final long serialVersionUID = -7320011705508155304L;

	public int m_stone_points;
	public int m_stone_count;
	public int m_number_of_possible_turns;
	int m_position_points;

	int m_teammate;
	int m_nemesis;
	int m_number;

	Stone m_stone[] = new Stone[Stone.STONE_COUNT_ALL_SHAPES];
	public Stone m_lastStone;

	public Player() {
		for (int i = 0; i < Stone.STONE_COUNT_ALL_SHAPES; i++){
			m_stone[i] = new Stone();
		}
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		Player c = (Player) super.clone();
		c.m_stone = m_stone.clone();
		for (int i = 0; i < Stone.STONE_COUNT_ALL_SHAPES; i++)
			c.m_stone[i] = (Stone)m_stone[i].clone();
		return c;
	}

	void init(Spiel spiel, int playernumber) {
		m_number = playernumber;
		for (int i = 0; i < Stone.STONE_COUNT_ALL_SHAPES; i++){
			m_stone[i].init(i);
		}
		refresh_data(spiel);
	}

	public final void copyFrom(Player from) {
		this.m_stone_points = from.m_stone_points;
		this.m_stone_count = from.m_stone_count;
		this.m_number_of_possible_turns = from.m_number_of_possible_turns;
		this.m_position_points = from.m_position_points;
		this.m_teammate = from.m_teammate;
		this.m_nemesis = from.m_nemesis;
		this.m_number = from.m_number;
		for (int i = 0; i < m_stone.length; i++) {
			this.m_stone[i].copyFrom(from.m_stone[i]);
		}
	}

	public int getPlayerNumber() {
		return m_number;
	}

	final public Stone get_stone(int n) {
		if (n < 0 || n > m_stone.length)
			return null;
		return m_stone[n];
	}

	void set_teammate(int player) {
		m_teammate = player;
	}

	void set_nemesis(int player) {
		m_nemesis = player;
	}


	void refresh_data(Spiel spiel) {
		m_stone_points = 0;
		m_number_of_possible_turns = 0;
		m_position_points = 0;
		m_stone_count = 0;

		for (int n = 0; n < Stone.STONE_COUNT_ALL_SHAPES; n++){
			Stone stone = m_stone[n];
			m_stone_count += stone.get_available();
		}

		for (int x = 0; x < spiel.m_field_size_x; x++){
			for (int y = 0; y < spiel.m_field_size_y; y++){
				if (spiel.get_game_field(m_number, y, x) == Stone.FIELD_ALLOWED){
					int turns_in_pos = 0;
					for (int n = 0; n < Stone.STONE_COUNT_ALL_SHAPES; n++){
						Stone stone = m_stone[n];
						if (stone.get_available() > 0){
							int turns;

							turns = stone.calculate_possible_turns_in_position(spiel, m_number, y, x);
							turns_in_pos += turns;
							m_position_points += turns * stone.get_stone_position_points() * stone.get_stone_points();
						}
					}
					m_number_of_possible_turns += turns_in_pos;
					if (turns_in_pos == 0) {
						/* there is no available turn in this position. mark as not allowed */
						spiel.set_field_free_bit(m_number, y, x);
					}
				} else if (spiel.get_game_field(y, x) == m_number)
					m_stone_points++;
			}
		}
		if (m_stone_count == 0 && m_lastStone != null) {
			m_stone_points += 15;
			if (m_lastStone.m_shape == 0)
				m_stone_points += 5;
		}
	}
}
