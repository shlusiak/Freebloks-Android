package de.saschahlusiak.freebloks.view;

import de.saschahlusiak.freebloks.controller.SpielClient;

public interface FreebloksViewInterface {
	public void setSpiel(SpielClient spiel);
	public SpielClient getSpiel();
	public void updateView();
}
