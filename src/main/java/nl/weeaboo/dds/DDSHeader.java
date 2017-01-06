/* JKTX
 * 
 * Copyright (c) 2011 Timon Bijlsma
 *   
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
   
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package nl.weeaboo.dds;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class DDSHeader {

	public static final int HEADER_LENGTH = 128;
	
	public static final byte[] FILE_IDENTIFIER = new byte[] {
		0x44, 0x44, 0x53, 0x20
	};		
	
	private int size;	
	private int flags;
	private int height;
	private int width;
	private int pitchOrLinearSize;
	private int depth;
	private int mipmapCount;
	private int[] reserved1 = new int[11];
	private DDSPixelFormat pf = new DDSPixelFormat();
	private int[] caps = new int[4];
	private int reserved2;
	
	public DDSHeader() {		
	}
	
	//Functions
	public void read(InputStream in) throws DDSFormatException, IOException {
		ByteBuffer buf = ByteBuffer.allocate(HEADER_LENGTH);
		DDSUtil.readFully(in, buf);
		read(buf);
	}
	
	public void read(ByteBuffer buf) throws DDSFormatException {
		ByteOrder oldOrder = buf.order();
		try {
			read0(buf);
		} catch (BufferUnderflowException bue) {
			throw new DDSFormatException("Unexpected end of input", bue);
		} finally {
			buf.order(oldOrder);
		}
	}
	
	private void read0(ByteBuffer buf) throws DDSFormatException {
		buf.order(ByteOrder.LITTLE_ENDIAN);
		
		//Check file identifier
		byte[] magic = new byte[FILE_IDENTIFIER.length];
		buf.get(magic);
		if (!Arrays.equals(magic, FILE_IDENTIFIER)) {
			throw new DDSFormatException("Input doesn't start with DDS magic value");
		}
		
		size = buf.getInt();
		flags = buf.getInt();
		height = buf.getInt();
		width = buf.getInt();
		pitchOrLinearSize = buf.getInt();
		depth = buf.getInt();
		mipmapCount = buf.getInt();
		for (int n = 0; n < 11; n++) {
			reserved1[n] = buf.getInt();
		}
		pf.read(buf);
		for (int n = 0; n < 4; n++) {
			caps[n] = buf.getInt();
		}
		reserved2 = buf.getInt();
	}
	
	public void write(OutputStream out) throws IOException {
		ByteBuffer buf = ByteBuffer.allocate(HEADER_LENGTH);		
		write(buf);
		out.write(buf.array());
	}
	
	public void write(ByteBuffer buf) {
		ByteOrder oldOrder = buf.order();
		try {
			write0(buf);
		} finally {
			buf.order(oldOrder);
		}
	}
	
	private void write0(ByteBuffer buf) {
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.put(FILE_IDENTIFIER);
		
		buf.putInt(size);
		buf.putInt(flags);
		buf.putInt(height);
		buf.putInt(width);
		buf.putInt(pitchOrLinearSize);
		buf.putInt(depth);
		buf.putInt(mipmapCount);
		for (int n = 0; n < 11; n++) {
			buf.putInt(reserved1[n]);
		}
		pf.write(buf);
		for (int n = 0; n < 4; n++) {
			buf.putInt(caps[n]);
		}
		buf.putInt(reserved2);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int n = 0; n < reserved1.length; n++) {
			if (n > 0) sb.append(" ");
			sb.append(String.format("%08x", reserved1[n]));
		}
		String reserved1String = sb.toString();
		
		sb.delete(0, sb.length());
		for (int n = 0; n < caps.length; n++) {
			if (n > 0) sb.append(" ");
			sb.append(String.format("%08x", caps[n]));
		}
		String capsString = sb.toString();
		
		return String.format("%s[size=%d, flags=0x%08x, height=%d, width=%d, pitchOrLinearSize=%d, " +
				"depth=%d, mipmapCount=%d, reserved1=[%s], pixelFormat=%s, caps=[%s], reserved2=%d]",
				getClass().getSimpleName(), size, flags, height, width, pitchOrLinearSize, depth,
				mipmapCount, reserved1String, pf, capsString, reserved2);
	}
		
	//Getters
	public int getSize() { return size; }
	public int getFlags() { return flags; }
	public boolean hasFlags(int f) { return (flags & f) == f; }
	public int getHeight() { return height; }
	public int getWidth() { return width; }
	public int getPitchOrLinearSize() { return pitchOrLinearSize; }
	public int getDepth() { return depth; }
	public int getMipmapCount() { return hasFlags(DDSConstants.DDSD_MIPMAPCOUNT) ? mipmapCount : 1; }
	public int[] getReserved1() { return reserved1.clone(); }
	public DDSPixelFormat getPixelFormat() { return pf; }
	public int[] getCaps() { return caps.clone(); }
	public int getReserved2() { return reserved2; }
	
}
