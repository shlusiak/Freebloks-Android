package de.saschahlusiak.freebloks.game;

import de.saschahlusiak.freebloks.model.Turn;

public interface ActivityInterface {
	boolean commitCurrentStone(Turn turn);
	void vibrate(int ms);
//	void gameFinished();
	void showPlayer(int player);
}
