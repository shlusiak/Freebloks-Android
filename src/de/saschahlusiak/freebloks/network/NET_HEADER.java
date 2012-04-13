package de.saschahlusiak.freebloks.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class NET_HEADER {
	/* int check1;	*/ /* uint8 */
	public int data_length; /* uint16 */
	public int msg_type; /* uint8 */
	/* int check2; */ /* uint8 */
	public static final int HEADER_SIZE = 5;
	
	byte buffer[];
	
	public NET_HEADER(int msg_type, int data_length) {
		this.msg_type = msg_type;
		this.data_length = data_length;
	}
	
	public NET_HEADER(NET_HEADER from) {
		this.data_length = from.data_length;
		this.msg_type = from.msg_type;
		this.buffer = from.buffer;
	}
	
	boolean read(Socket socket) throws Exception {
		InputStream is;
		int r;
		int check1, check2;
		if (socket == null)
			return false;
		if (socket.isInputShutdown())
			return false;

		buffer = new byte[HEADER_SIZE];
		is = socket.getInputStream();

		if (is.available() < HEADER_SIZE)
			return false;

		r = is.read(buffer, 0, HEADER_SIZE);
		if (r < HEADER_SIZE)
			throw new Exception("Short read for header");

		check1 = buffer[0];
		if (check1 < 0)
			check1 += 256;
		data_length = buffer[1] << 8 | buffer[2];
		msg_type = buffer[3];
		check2 = buffer[4];
		if (check2 < 0)
			check2 += 256;

		if (data_length < HEADER_SIZE)
			throw new Exception("Invalid length in header data");

		/* Beiden Checksums erneut berechnen */
		int c1 = (byte) (data_length & 0x0055) ^ msg_type;
		int c2 = (c1 ^ 0xD6) + msg_type;
		/* Bei Ungleichheit Fehler, sonst Nachricht ok */
		if (c1 != check1 || c2 != check2)
			throw new Exception("header checksum failed");

		if (is.available() < (data_length - HEADER_SIZE))
			throw new Exception("short read for remaining package payload");
		buffer = new byte[data_length - HEADER_SIZE];

		r = is.read(buffer, 0, data_length - HEADER_SIZE);
		if (r < data_length - HEADER_SIZE)
			throw new Exception("short read for remaining package payload");

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
	
	public boolean send(Socket socket) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		prepare(bos);
		try {
			socket.getOutputStream().write(bos.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
