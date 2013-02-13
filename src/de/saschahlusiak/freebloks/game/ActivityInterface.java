package de.saschahlusiak.freebloks.game;

import de.saschahlusiak.freebloks.model.Stone;

public interface ActivityInterface {
	public boolean commitCurrentStone(Stone stone, int x, int y);
	public void vibrate(int ms);
	public void gameFinished();
}
