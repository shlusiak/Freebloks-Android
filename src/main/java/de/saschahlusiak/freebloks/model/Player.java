package de.saschahlusiak.freebloks.model;

import java.io.Serializable;

import static de.saschahlusiak.freebloks.model.Stone.FIELD_ALLOWED;
import static de.saschahlusiak.freebloks.model.Stone.STONE_FIELD_ALLOWED;

public class Player implements Serializable, Cloneable {
	private static final long serialVersionUID = -7320011705508155304L;

	public int m_stone_points;
	public int m_stone_count;
	public int m_number_of_possible_turns;
	int m_position_points;

	int m_teammate;
	int m_nemesis;
	int m_number;

	Stone m_stone[] = new Stone[StoneType.COUNT];
	public Stone m_lastStone;

	public Player() {
		for (int i = 0; i < StoneType.COUNT; i++){
			m_stone[i] = new Stone();
		}
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		Player c = (Player) super.clone();
		c.m_stone = m_stone.clone();
		for (int i = 0; i < StoneType.COUNT; i++)
			c.m_stone[i] = (Stone)m_stone[i].clone();
		return c;
	}

	void init(Spiel spiel, int playernumber) {
		m_number = playernumber;
		for (int i = 0; i < StoneType.COUNT; i++){
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

		for (int n = 0; n < StoneType.COUNT; n++){
			Stone stone = m_stone[n];
			m_stone_count += stone.getAvailableCount();
		}

		for (int x = 0; x < spiel.width; x++){
			for (int y = 0; y < spiel.height; y++){
				if (spiel.getFieldStatus(m_number, y, x) == FIELD_ALLOWED){
					int turns_in_pos = 0;
					for (int n = 0; n < StoneType.COUNT; n++){
						Stone stone = m_stone[n];
						if (stone.getAvailableCount() > 0){
							int turns;

							turns = calculate_possible_turns_in_position(spiel, stone, y, x);
							turns_in_pos += turns;
							m_position_points += turns * stone.getPositionPoints() * stone.getPoints();
						}
					}
					m_number_of_possible_turns += turns_in_pos;
					if (turns_in_pos == 0) {
						/* there is no available turn in this position. mark as not allowed */
						spiel.clearAllowedBit(m_number, y, x);
					}
				} else if (spiel.getFieldPlayer(y, x) == m_number)
					m_stone_points++;
			}
		}
		if (m_stone_count == 0 && m_lastStone != null) {
			m_stone_points += 15;
			if (m_lastStone.getShape() == 0)
				m_stone_points += 5;
		}
	}

	private final int calculate_possible_turns_in_position(Spiel spiel, Stone stone, int fieldY, int fieldX) {
		int count = 0;

		int mirror, mirror_max;
		int rotate;

		if (stone.getMirrorable() == Mirrorable.Important)
			mirror_max = 1;
		else
			mirror_max = 0;

		for (mirror = 0; mirror <= mirror_max; mirror++) {
			for (rotate = 0; rotate < stone.getRotateable().getValue(); rotate++) {
				for (int x = 0; x < stone.getSize(); x++) {
					for (int y = 0; y < stone.getSize(); y++) {
						if (stone.getStoneField(y, x, mirror, rotate) == STONE_FIELD_ALLOWED) {
							if (spiel.isValidTurn(stone, m_number, fieldY - y, fieldX - x, mirror, rotate) == FIELD_ALLOWED) {
								count++;
							}
						}
					}
				}
			}
		}

		return count;
	}
}
