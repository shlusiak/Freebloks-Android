package de.saschahlusiak.freebloks.view;

import de.saschahlusiak.freebloks.controller.SpielClient;
import de.saschahlusiak.freebloks.game.ActivityInterface;
import de.saschahlusiak.freebloks.model.Stone;

public interface ViewInterface {
	void setActivity(ActivityInterface activity);
	void setSpiel(SpielClient spiel);
	SpielClient getSpiel();
	void updateView();
}
