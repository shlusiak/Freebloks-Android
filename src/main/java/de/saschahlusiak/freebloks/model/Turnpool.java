package de.saschahlusiak.freebloks.model;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * Simple Stack to contain Turns for the undo history
 */
public class Turnpool extends LinkedList<Turn> implements Serializable {
	private static final long serialVersionUID = 4356065376532513833L;
}
