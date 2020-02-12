package de.saschahlusiak.freebloks.network;

import java.io.ByteArrayOutputStream;

public class NET_CHAT extends NET_HEADER {
	public int client; /* int 8 */
	public int length; /* uint 8 */
	public String text; /* uint8[] */

	public NET_CHAT(String text) {
		this(text, 0);
	}

	public NET_CHAT(String text, int client) {
		super(MessageType.Chat, 3 + text.length());
		this.text = text;
		this.length = text.length();
		this.client = client;
	}

	@Override
	void prepare(ByteArrayOutputStream bos) {
		super.prepare(bos);
		bos.write(client);
		bos.write(length);
		for (int i = 0; i < text.length(); i++)
			bos.write((int)text.charAt(i));
		bos.write(0);
	}
}
