package de.saschahlusiak.freebloks.network;

public class NET_REQUEST_UNDO extends NET_HEADER {

	public NET_REQUEST_UNDO() {
		super(MessageType.RequestUndo, 0);
	}
}
