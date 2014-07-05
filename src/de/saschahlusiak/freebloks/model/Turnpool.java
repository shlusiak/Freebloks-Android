package de.saschahlusiak.freebloks.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Turnpool implements Serializable {
	private static final long serialVersionUID = 4356065376532513833L;

	ArrayList<Turn> turns = new ArrayList<Turn>();
	int current = 0;

	final public void add_turn(Turn turn) {
		current++;
		if (turns.size() < current) {
			turns.add(new Turn(turn));
		} else {
			turns.get(current - 1).copy(turn);
		}
	}

	final public void add_turn(int playernumber, Stone stone, int y, int x) {
		current++;
		if (turns.size() < current){
			turns.add(new Turn(playernumber, stone, y, x));
		} else {
			turns.get(current - 1).init(playernumber, stone, y, x);
		}
	}

	final void begin_add() {
		current = 0;
	}

	final public void delete_all_turns() {
		current = 0;
		turns.clear();
	}

	final void delete_last() {
		if (current <= 0)
			return;
		current--;
		turns.remove(current);
	}

	final public Turn get_last_turn() {
		return turns.get(current - 1);
	}

	final Turn get_turn(int i) {
		return turns.get(i - 1);
	}

	final int get_number_of_stored_turns() {
		return current;
	}
}
