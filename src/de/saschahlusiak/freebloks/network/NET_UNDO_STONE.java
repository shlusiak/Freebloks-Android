package de.saschahlusiak.freebloks.network;

public class NET_UNDO_STONE extends NET_SET_STONE {

	public NET_UNDO_STONE() {
		super(Network.MSG_UNDO_STONE);
	}

	public NET_UNDO_STONE(NET_HEADER from) {
		super(from);
	}
}
