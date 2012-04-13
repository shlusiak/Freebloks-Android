package de.saschahlusiak.freebloks.controller;

import de.saschahlusiak.freebloks.model.Stone;
import de.saschahlusiak.freebloks.model.Turn;
import de.saschahlusiak.freebloks.network.NET_CHAT;
import de.saschahlusiak.freebloks.network.NET_SERVER_STATUS;
import de.saschahlusiak.freebloks.network.NET_SET_STONE;

public interface SpielClientInterface {
	void newCurrentPlayer(int player);
	void stoneWasSet(NET_SET_STONE s);
	void hintReceived(NET_SET_STONE s);
	void gameFinished();
	void chatReceived(NET_CHAT c);
	void gameStarted();
	void stoneUndone(Stone s, Turn t);
	void serverStatus(NET_SERVER_STATUS status);
}
