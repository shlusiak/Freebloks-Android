package de.saschahlusiak.freebloks.network;

public class NET_START_GAME extends NET_HEADER {
	public NET_START_GAME() {
		super(MessageType.StartGame, 0);
	}
	
	public NET_START_GAME(NET_HEADER from) {
		super(from);
	}
}
