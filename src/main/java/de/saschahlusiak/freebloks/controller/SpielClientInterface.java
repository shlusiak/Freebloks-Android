package de.saschahlusiak.freebloks.controller;

import android.support.annotation.NonNull;

import de.saschahlusiak.freebloks.model.Spiel;
import de.saschahlusiak.freebloks.model.Turn;
import de.saschahlusiak.freebloks.network.NET_CHAT;
import de.saschahlusiak.freebloks.network.NET_SERVER_STATUS;
import de.saschahlusiak.freebloks.network.NET_SET_STONE;

public interface SpielClientInterface {
	void onConnected(@NonNull Spiel spiel);
	void onDisconnected(@NonNull Spiel spiel);

	void newCurrentPlayer(int player);
	void stoneWillBeSet(@NonNull NET_SET_STONE s);
	void stoneHasBeenSet(@NonNull NET_SET_STONE s);
	void hintReceived(@NonNull NET_SET_STONE s);
	void gameFinished();
	void chatReceived(@NonNull NET_CHAT c);
	void gameStarted();
	void stoneUndone(@NonNull Turn t);
	void serverStatus(@NonNull NET_SERVER_STATUS status);
}
