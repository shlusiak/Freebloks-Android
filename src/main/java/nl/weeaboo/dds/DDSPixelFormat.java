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
import java.io.UnsupportedEncodingException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DDSPixelFormat {

	public static final int PIXEL_FORMAT_LENGTH = 32;
	
	private int size = PIXEL_FORMAT_LENGTH;
	private int flags;
	private int fourCC;
	private int rgbBitCount;
	private int rBitMask;
	private int gBitMask;
	private int bBitMask;
	private int aBitMask;
	
	public DDSPixelFormat() {		
	}
	
	//Functions
	public void read(InputStream in) throws DDSFormatException, IOException {
		ByteBuffer buf = ByteBuffer.allocate(PIXEL_FORMAT_LENGTH);
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

		size = buf.getInt();
		if (size != PIXEL_FORMAT_LENGTH) {
			throw new DDSFormatException("Unexpected size for pixel format: " + size);
		}
		
		flags = buf.getInt();
		fourCC = buf.getInt();
		rgbBitCount = buf.getInt();
		rBitMask = buf.getInt();
		gBitMask = buf.getInt();
		bBitMask = buf.getInt();
		aBitMask = buf.getInt();
	}
	
	public void write(OutputStream out) throws IOException {
		ByteBuffer buf = ByteBuffer.allocate(size);		
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
		
		buf.putInt(size);
		buf.putInt(flags);
		buf.putInt(fourCC);
		buf.putInt(rgbBitCount);
		buf.putInt(rBitMask);
		buf.putInt(gBitMask);
		buf.putInt(bBitMask);
		buf.putInt(aBitMask);
	}

	@Override
	public String toString() {
		return String.format("%s[size=%d, flags=%d, fourCC='%s', rgbBitCount=%d, " +
				"rBitMask=0x%x, gBitMask=0x%x, bBitMask=0x%x, aBitMask=0x%x]",
				getClass().getSimpleName(), size, flags, getFourCCString(), rgbBitCount,
				rBitMask, gBitMask, bBitMask, aBitMask);
	}
		
	//Getters
	public int getSize() { return size; }
	public int getFlags() { return flags; }
	public boolean hasFlags(int f) { return (flags & f) == f; }
	public int getFourCC() { return fourCC; }
	public String getFourCCString() {
		byte[] bytes = new byte[4];
		bytes[0] = (byte)(fourCC    );
		bytes[1] = (byte)(fourCC>>8);
		bytes[2] = (byte)(fourCC>>16);
		bytes[3] = (byte)(fourCC>>24);
		try {
			return new String(bytes, 0, bytes.length, "ASCII");
		} catch (UnsupportedEncodingException e) {
			return new String(bytes, 0, bytes.length);
		}
	}
	public int getRGBBitCount() { return rgbBitCount; }
	public int getRBitMask() { return rBitMask; }
	public int getGBitMask() { return gBitMask; }
	public int getBBitMask() { return bBitMask; }
	public int getABitMask() { return aBitMask; }
	
}
