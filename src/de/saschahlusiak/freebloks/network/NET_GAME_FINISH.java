package de.saschahlusiak.freebloks.network;

public class NET_GAME_FINISH extends NET_HEADER {

	public NET_GAME_FINISH() {
		super(Network.MSG_GAME_FINISH, 0);
	}
	public NET_GAME_FINISH(NET_HEADER from) {
		super(from);
	}
}
