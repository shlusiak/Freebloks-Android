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
		super(Network.MSG_CHAT, 3 + text.length());
		this.text = text;
		this.length = text.length();
		this.client = client;
	}

	public NET_CHAT(NET_HEADER from) {
		super(from);
		client = data[0];
		length = unsigned(data[1]);

		while ((data[length + 1] == (byte)'\0') || (data[length +1] == (byte)'\n') || (data[length + 1]== (byte)'\r'))  {
			length--;
			data_length--;
		}

		char[] c = new char[length]; /* ignore the trailing \0 */
		for (int i = 0; i < c.length; i++)
			c[i] = (char)unsigned(data[i + 2]);
		text = String.copyValueOf(c);
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
