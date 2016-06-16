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

public class DDSHeader10 {

	public static final int HEADER_LENGTH = 20;
	
	private int dxgiFormat;
	private int resourceDimension;
	private int miscFlag;	
	private int arraySize;
	private int reserved;
	  
	public DDSHeader10() {		
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
		
		dxgiFormat = buf.getInt();
		resourceDimension = buf.getInt();
		miscFlag = buf.getInt();
		arraySize = buf.getInt();
		reserved = buf.getInt();
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
		
		buf.putInt(dxgiFormat);
		buf.putInt(resourceDimension);
		buf.putInt(miscFlag);
		buf.putInt(arraySize);
		buf.putInt(reserved);
	}

	@Override
	public String toString() {
		return String.format("%s[dxgiFormat=%d, resourceDimension=%d, miscFlag=0x%08x, " +
				"arraySize=%d, reserved=%d]",
				getClass().getSimpleName(), dxgiFormat, resourceDimension, miscFlag, arraySize, reserved);
	}
		
	//Getters
	public int getDXGIFormat() { return dxgiFormat; }
	public int getResourceDimension() { return resourceDimension; }
	public int getMiscFlag() { return miscFlag; }
	public int getArraySize() { return arraySize; }
	public int getReserved() { return reserved; }

}
