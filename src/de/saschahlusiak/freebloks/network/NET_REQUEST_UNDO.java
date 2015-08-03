package de.saschahlusiak.freebloks.network;

public class NET_REQUEST_UNDO extends NET_HEADER {

	public NET_REQUEST_UNDO() {
		super(Network.MSG_REQUEST_UNDO, 0);
	}
	
	public NET_REQUEST_UNDO(NET_HEADER from) {
		super(from);
	}
}
