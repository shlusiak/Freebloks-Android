package de.saschahlusiak.freebloks.network;

public class NET_REQUEST_PLAYER extends NET_HEADER {

	public NET_REQUEST_PLAYER() {
		super(Network.MSG_REQUEST_PLAYER, 0);
	}
	public NET_REQUEST_PLAYER(NET_HEADER from) {
		super(from);
	}
}
