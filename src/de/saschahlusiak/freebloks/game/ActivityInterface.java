package de.saschahlusiak.freebloks.game;

import de.saschahlusiak.freebloks.controller.SpielClient;
import de.saschahlusiak.freebloks.model.Stone;

public interface ActivityInterface {
	public void selectCurrentStone(SpielClient spiel, Stone stone);
	public boolean commitCurrentStone(SpielClient spiel, Stone stone, int x, int y);
}
