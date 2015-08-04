package de.saschahlusiak.freebloks.game;

import de.saschahlusiak.freebloks.model.Stone;
import de.saschahlusiak.freebloks.model.Turn;

public interface ActivityInterface {
	public boolean commitCurrentStone(Turn turn);
	public void vibrate(int ms);
	public void gameFinished();
	public void showPlayer(int player);
}
