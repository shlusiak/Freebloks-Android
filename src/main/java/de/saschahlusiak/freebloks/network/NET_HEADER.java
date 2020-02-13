package de.saschahlusiak.freebloks.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

public class NET_HEADER implements Serializable {
	private static final long serialVersionUID = 1L;

	/* int check1;	*/ /* uint8 */
	private int data_length; /* uint16 */
	private int msg_type; /* uint8 */
	/* int check2; */ /* uint8 */
	private static final int HEADER_SIZE = 5;

	public NET_HEADER(MessageType msg_type, int data_length) {
		this.msg_type = msg_type.getRawValue();
		this.data_length = data_length;
	}

	void prepare(ByteArrayOutputStream bos) {
		int l = data_length + HEADER_SIZE;
		int check1, check2;
		check1 = (l & 0x0055) ^ msg_type;
		check2 = (check1 ^ 0xD6) + msg_type;

		bos.write(check1);
		bos.write((l >> 8) & 0xff);
		bos.write(l & 0xff);
		bos.write(msg_type);
		bos.write(check2);
	}

	public boolean send(OutputStream os) {
		if (os == null)
			return false;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		prepare(bos);
		try {
			os.write(bos.toByteArray());
		} catch (IOException e) {
			// this is usually a broken pipe exception, which happens when the connection
			// is closed. This is non-fatal here and does not need to be logged.
			return false;
		}
		return true;
	}
}
