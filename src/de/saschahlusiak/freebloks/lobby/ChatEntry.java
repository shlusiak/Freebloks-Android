package de.saschahlusiak.freebloks.lobby;

public class ChatEntry {
	int client;
	String text, name;
	
	public ChatEntry(int client, String text, String name) {
		this.client = client;
		this.text = text;
		if (name != null)
			this.name = name;
		else
			name = "Client " + client;
	}
	
	@Override
	public String toString() {
		if (client < 0)
			return "* " + text;
		return name + ": " + text;
	}
}
