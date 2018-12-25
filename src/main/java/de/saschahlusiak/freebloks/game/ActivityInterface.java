package de.saschahlusiak.freebloks.game;

import android.support.annotation.NonNull;

import de.saschahlusiak.freebloks.model.Turn;

public interface ActivityInterface {
	boolean commitCurrentStone(@NonNull Turn turn);
	void vibrate(int ms);
//	void gameFinished();
	void showPlayer(int player);
}
