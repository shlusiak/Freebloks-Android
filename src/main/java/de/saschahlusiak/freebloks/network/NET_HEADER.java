package de.saschahlusiak.freebloks.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

public class NET_HEADER implements Serializable {
	private static final long serialVersionUID = 1L;

	/* int check1;	*/ /* uint8 */
	public int data_length; /* uint16 */
	public int msg_type; /* uint8 */
	/* int check2; */ /* uint8 */
	public static final int HEADER_SIZE = 5;

	byte header[];
	byte data[];
	
	NET_HEADER() {
		this(0, 0);
	}

	public NET_HEADER(int msg_type, int data_length) {
		this.msg_type = msg_type;
		this.data_length = data_length;
	}

	public NET_HEADER(NET_HEADER from) {
		this.data_length = from.data_length;
		this.msg_type = from.msg_type;
		this.data = from.data;
	}

	/**
	 * a java byte is always signed, being -128..127
	 * casting (byte)-1 to int will result in (int)-1
	 *
	 */
	public static int unsigned(byte b) {
		return (b & 0xFF);
	}

	boolean read(InputStream is, boolean block) throws ProtocolException,IOException {
		int r;
		int check1, check2;

		header = new byte[HEADER_SIZE];

		if (!block && (is.available() < HEADER_SIZE))
			return false;

		r = is.read(header, 0, HEADER_SIZE);
		if (r == -1)
			throw new IOException("EOF when reading packet header");

		if (r < HEADER_SIZE)
			throw new IOException(String.format("short read: %d out of %d", r, HEADER_SIZE));

		check1 = unsigned(header[0]);
		data_length = unsigned(header[1]) << 8 | unsigned(header[2]);
		msg_type = header[3];
		check2 = unsigned(header[4]);

		if (data_length < HEADER_SIZE)
			throw new ProtocolException("invalid header header length: " + data_length);

		/* Beiden Checksums erneut berechnen */
		int c1 = (byte) (data_length & 0x0055) ^ msg_type;
		int c2 = (c1 ^ 0xD6) + msg_type;
		/* Bei Ungleichheit Fehler, sonst Nachricht ok */
		if (c1 != check1 || c2 != check2)
			throw new ProtocolException("header checksum failed");

		data_length -= HEADER_SIZE;

		data = new byte[data_length];
		int offset = 0;

		do {
			r = is.read(data, offset, data_length - offset);
			if (r == -1)
				throw new IOException("EOF when reading packet payload");

			offset += r;
		} while (offset < data_length);

		return true;
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
