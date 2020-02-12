package de.saschahlusiak.freebloks.controller;

import androidx.annotation.NonNull;

import de.saschahlusiak.freebloks.model.Spiel;
import de.saschahlusiak.freebloks.model.Turn;
import de.saschahlusiak.freebloks.network.message.MessageServerStatus;

public interface SpielClientInterface {
	void onConnected(@NonNull Spiel spiel);
	void onDisconnected(@NonNull Spiel spiel);

	void newCurrentPlayer(int player);
	void stoneWillBeSet(@NonNull Turn turn);
	void stoneHasBeenSet(@NonNull Turn turn);
	void hintReceived(@NonNull Turn turn);
	void gameFinished();
	void chatReceived(int client, @NonNull String message);
	void gameStarted();
	void stoneUndone(@NonNull Turn t);
	void serverStatus(@NonNull MessageServerStatus status);
}
