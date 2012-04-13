package de.saschahlusiak.freebloks.network;

import java.io.ByteArrayOutputStream;

public class NET_CHAT extends NET_HEADER {
	public int client; /* int 8 */
	public int length; /* int 8 */
	public String text; /* uint8[] */

	public NET_CHAT(String text) {
		super(Network.MSG_CHAT, 2 + text.length());
		this.text = text;
	}

	public NET_CHAT(NET_HEADER from) {
		super(from);
		client = buffer[0];
		length = buffer[1];
		
		while ((buffer[length + 1] == (byte)'\0') || (buffer[length +1] == (byte)'\n') || (buffer[length + 1]== (byte)'\r')) 
			length--;
		
		char[] c = new char[length]; /* ignore the trailing \0 */
		for (int i = 0; i < c.length; i++)
			c[i] = (char)buffer[i + 2];
		text = String.copyValueOf(c);
	}

	@Override
	void prepare(ByteArrayOutputStream bos) {
		super.prepare(bos);
		bos.write(client);
		bos.write(length);
		for (int i = 0; i < text.length(); i++)
			bos.write((int)text.charAt(i));
	}

}
