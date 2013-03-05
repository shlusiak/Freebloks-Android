package de.saschahlusiak.freebloks.game;

import de.saschahlusiak.freebloks.model.Stone;

public interface ActivityInterface {
	public boolean commitCurrentStone(Stone stone, int x, int y);
	public void vibrate_on_move(int ms);
	public void vibrate_on_place(int ms);
	public void gameFinished();
}
