package de.saschahlusiak.freebloks.game;

import de.saschahlusiak.freebloks.model.Player;
import de.saschahlusiak.freebloks.model.Spiel;
import de.saschahlusiak.freebloks.model.Stone;

public interface ActivityInterface {
	public void selectCurrentStone(Spiel spiel, Stone stone);
}
