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

package nl.weeaboo.jktx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class KTXFile {

	private KTXHeader header;
	private KTXMetaData meta;
	private KTXTextureData textureData;
	
	public KTXFile() {
		clear0();
	}
	
	//Functions
	public void clear() {
		clear0();
	}
	
	private void clear0() {
		header = new KTXHeader();
		meta = new KTXMetaData();
		textureData = new KTXTextureData();		
	}

	public void read(File file) throws KTXFormatException, IOException {
		InputStream fin = new FileInputStream(file);
		try {
			read(fin);
		} finally {
			fin.close();
		}
	}
	
	public void read(InputStream in) throws KTXFormatException, IOException {
		clear();

		header.read(in);
		meta.read(in, header.getByteOrder(), header.getBytesOfKeyValueData());
		textureData.readMipmaps(in, header.getByteOrder(), header.getGLTypeSize(),
				Math.max(1, header.getNumberOfMipmapLevels()),
				Math.max(1, header.getNumberOfArrayElements()),
				header.getNumberOfFaces());
	}
	
	public void write(File file) throws IOException {
		FileOutputStream fout = new FileOutputStream(file);
		try {
			write(fout);
		} finally {
			fout.close();
		}
	}
	
	public void write(OutputStream out) throws IOException {
		header.setBytesOfKeyValueData(meta.calculateRequiredBytes());
		
		header.write(out);
		meta.write(out);
		textureData.writeMipmaps(out, header.getByteOrder(), header.getGLTypeSize());
	}
	
	@Override
	public String toString() {
		return String.format("%s:\n\theader=%s\n\tmeta=%s\n\ttextureData=%s", getClass().getSimpleName(),
				header, meta, textureData);
	}
	
	//Getters
	public KTXHeader getHeader() {
		return header;
	}
	public KTXMetaData getMetaData() {
		return meta;
	}
	public KTXTextureData getTextureData() {
		return textureData;
	}
	
	//Setters
	
}
