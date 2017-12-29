package de.saschahlusiak.freebloks.model;

import java.io.Serializable;
import java.util.Deque;
import java.util.LinkedList;

/**
 * Simple Stack to contain Turns for the undo history
 */
public class Turnpool implements Serializable {
	private static final long serialVersionUID = 4356065376532513833L;

	private final Deque<Turn> turns = new LinkedList<>();

	public final void add(Turn turn) {
		turns.add(turn);
	}

	public final void clear() {
		turns.clear();
	}

	public final Turn pop() {
		return turns.pollLast();
	}

	public final Turn get() {
		return turns.peekLast();
	}
}
