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

import static nl.weeaboo.dds.DDSConstants.FOURCC_DXT1;
import static nl.weeaboo.dds.DDSConstants.FOURCC_DXT3;
import static nl.weeaboo.dds.DDSConstants.FOURCC_DXT5;
import static nl.weeaboo.dds.DDSConstants.FOURCC_ATCI;
import static nl.weeaboo.dds.DDSConstants.FOURCC_ATCA;
import static nl.weeaboo.dds.DDSConstants.FOURCC_ETC;
import static nl.weeaboo.jktx.GLConstants.GL_COMPRESSED_RGBA_S3TC_DXT1_EXT;
import static nl.weeaboo.jktx.GLConstants.GL_COMPRESSED_RGBA_S3TC_DXT3_EXT;
import static nl.weeaboo.jktx.GLConstants.GL_COMPRESSED_RGBA_S3TC_DXT5_EXT;
import static nl.weeaboo.jktx.GLConstants.GL_ATC_RGBA_INTERPOLATED_ALPHA_AMD;
import static nl.weeaboo.jktx.GLConstants.GL_ATC_RGBA_EXPLICIT_ALPHA_AMD;
import static nl.weeaboo.jktx.GLConstants.GL_ETC1_RGB8_OES;
import static nl.weeaboo.jktx.GLConstants.GL_RGBA;
import static nl.weeaboo.jktx.GLConstants.GL_RGB;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import nl.weeaboo.jktx.GLConstants;
import nl.weeaboo.jktx.KTXFile;
import nl.weeaboo.jktx.KTXHeader;
import nl.weeaboo.jktx.KTXTextureData;

public class DDSFile {

	private DDSHeader header;
	private DDSHeader10 header10;
	private MipmapData[] mipmaps;
	
	public DDSFile() {
		clear0();
	}
	
	//Functions
	public void clear() {
		clear0();
	}
	
	private void clear0() {
		header = new DDSHeader();
		header10 = new DDSHeader10();
		mipmaps = null;
	}
	
	public void read(File file) throws DDSFormatException, IOException {
		FileInputStream fin = new FileInputStream(file);
		try {
			read(fin);
		} finally {
			fin.close();
		}
	}
	
	public void read(InputStream in) throws DDSFormatException, IOException {
		clear();

		header.read(in);
		if (hasHeader10()) {
			header10.read(in);
		}
		
		DDSPixelFormat pf = header.getPixelFormat();
		int w = header.getWidth();
		int h = header.getHeight();
		if (header.getDepth() > 1) {
			throw new DDSFormatException("Only 2D textures are supported");
		}
		
		int blockSize = 1;
		int blockBytes = (pf.getRGBBitCount()+7)/8;
		if (pf.hasFlags(DDSConstants.DDPF_FOURCC)) {
			blockSize = DDSUtil.getCompressedBlockPixels(pf.getFourCC());
			blockBytes = DDSUtil.getCompressedBlockBytes(pf.getFourCC());
		}
		
		int levels = header.getMipmapCount();
		mipmaps = new MipmapData[levels];
		for (int level = 0; level < levels; level++) {
			int mw = Math.max(1, ((w>>level)+blockSize-1)/blockSize);
			int mh = Math.max(1, ((h>>level)+blockSize-1)/blockSize);
			int bytesPerRow = mw * blockBytes;
			
			ByteBuffer data = ByteBuffer.allocateDirect(bytesPerRow * mh);
			data.order(ByteOrder.nativeOrder());
			DDSUtil.readFully(in, data);
			
			mipmaps[level] = new MipmapData(data, bytesPerRow);
		}
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
		header.write(out);
		if (hasHeader10()) {
			header10.write(out);
		}
	}
	
	public void toKTX(KTXFile ktx, boolean supportBGRA, boolean supportUINT8888) throws DDSFormatException {
		ktx.clear();
		
		if (header.getDepth() != 0) {
			throw new DDSFormatException("Textures with depth aren't supported yet");			
		}
		
		KTXHeader kh = ktx.getHeader();
		kh.setDimensions(header.getWidth(), header.getHeight(), header.getDepth());			
		kh.setNumberOfMipmapLevels(header.getMipmapCount());
		
		KTXTextureData ktdata = ktx.getTextureData();
		
		DDSPixelFormat pf = header.getPixelFormat();
		if (pf.hasFlags(DDSConstants.DDPF_FOURCC)) {
			int glInternalFormat, glBaseInternalFormat;
			switch (pf.getFourCC()) {
			case FOURCC_DXT1:
				glInternalFormat = GL_COMPRESSED_RGBA_S3TC_DXT1_EXT;
				glBaseInternalFormat = GL_RGBA;
				break;
			case FOURCC_DXT3:
				glInternalFormat = GL_COMPRESSED_RGBA_S3TC_DXT3_EXT;
				glBaseInternalFormat = GL_RGBA;
				break;
			case FOURCC_DXT5:
				glInternalFormat = GL_COMPRESSED_RGBA_S3TC_DXT5_EXT;
				glBaseInternalFormat = GL_RGBA;
				break;
			case FOURCC_ATCI:
				glInternalFormat = GL_ATC_RGBA_INTERPOLATED_ALPHA_AMD;
				glBaseInternalFormat = GL_RGBA;
				break;
			case FOURCC_ATCA:
				glInternalFormat = GL_ATC_RGBA_EXPLICIT_ALPHA_AMD;
				glBaseInternalFormat = GL_RGBA;
				break;
			case FOURCC_ETC:
				glInternalFormat = GL_ETC1_RGB8_OES;
				glBaseInternalFormat = GL_RGB;
				break;
			default: throw new DDSFormatException("Unsupported pixel format: " + header.getPixelFormat());
			}
			
			kh.setCompressedGLFormat(glInternalFormat, glBaseInternalFormat);
			
			for (int level = 0; level < header.getMipmapCount(); level++) {
				ktdata.setMipmapLevel(level, getDataBuffer(level));
			}
		} else {
			int bits = pf.getRGBBitCount();
			int amask = pf.getABitMask();
			int rmask = pf.getRBitMask();
			int gmask = pf.getGBitMask();
			int bmask = pf.getBBitMask();
			int ashift = DDSUtil.calculateMaskShift(amask);
			int rshift = DDSUtil.calculateMaskShift(rmask);
			int gshift = DDSUtil.calculateMaskShift(gmask);
			int bshift = DDSUtil.calculateMaskShift(bmask);
			
			int fmt = (supportBGRA ? GLConstants.GL_BGRA : GLConstants.GL_RGBA);
			int type, typeSize;
			if (supportUINT8888) {
				type = GLConstants.GL_UNSIGNED_INT_8_8_8_8_REV;
				typeSize = 4;
			} else {
				type = GLConstants.GL_UNSIGNED_BYTE;
				typeSize = 1;
			}
			kh.setGLFormat(GLConstants.GL_RGBA8, GLConstants.GL_RGBA, fmt, type, typeSize);
			
			for (int level = 0; level < header.getMipmapCount(); level++) {
				int mw = Math.max(1, header.getWidth()>>level);
				int mh = Math.max(1, header.getHeight()>>level);
				
				int outBytesPerRow = mw * 4; //No need to align, 32-bit pixels are always aligned
				int rowpad = 0; //Difference between outBytesPerRow and align4(outBytesPerRow)
				if ((outBytesPerRow & 3) != 0) {
					throw new RuntimeException("Internal output row alignment error: " + outBytesPerRow);
				}
				
				ByteBuffer out = ByteBuffer.allocateDirect(outBytesPerRow * mh);
				out.order(ByteOrder.nativeOrder());
				
				ByteBuffer data = getDataBuffer(level);
				data.mark();			
				for (int y = 0; y < mh; y++) {
					for (int x = 0; x < mw; x++) {
						int pixel;
						switch (bits) {
						case 8:  pixel = data.get() & 0xFF; break;
						case 16: pixel = data.getShort() & 0xFFFF; break;
						case 24: {
							pixel = data.getShort() & 0xFFFF;
							if (data.order() == ByteOrder.LITTLE_ENDIAN) {
								pixel = ((data.get()&0xFF)<<16)|pixel;
							} else {
								pixel = (pixel<<8)|(data.get()&0xFF);
							}
							break;
						}
						case 32: pixel = data.getInt(); break;
						default: throw new DDSFormatException("Unsupported bit depth: " + bits);
						}
						
						int a = Math.max(0, Math.min(255, (pixel & amask) >>> ashift));
						int r = Math.max(0, Math.min(255, (pixel & rmask) >>> rshift));
						int g = Math.max(0, Math.min(255, (pixel & gmask) >>> gshift));
						int b = Math.max(0, Math.min(255, (pixel & bmask) >>> bshift));
						
						if (fmt == GLConstants.GL_BGRA) {
							out.putInt((a<<24)|(r<<16)|(g<<8)|(b));
						} else {
							out.put((byte)r);
							out.put((byte)g);
							out.put((byte)b);
							out.put((byte)a);
						}
					}
					out.position(out.position() + rowpad);
				}
				data.reset();
				out.rewind();
				
				ktdata.setMipmapLevel(level, out);
			}
		}
	}
	
	@Override
	public String toString() {
		return String.format("%s:\n\theader=%s\n\theader10=%s]",
				getClass().getSimpleName(), header, header10);
	}
	
	//Getters
	public DDSHeader getHeader() {
		return header;
	}
	public DDSHeader10 getHeader10() {
		return header10;
	}
	public boolean hasHeader10() {
		DDSPixelFormat pf = header.getPixelFormat();
		return pf.hasFlags(DDSConstants.DDPF_FOURCC) && pf.getFourCC() == DDSConstants.FOURCC_DX10;
	}
	public ByteBuffer getDataBuffer(int level) {
		return mipmaps[level].getDataBuffer();
	}
	public int getRowBytes(int level) {
		return mipmaps[level].getRowBytes();
	}
	public int getDataBytes(int level) {
		return mipmaps[level].getDataBytes();
	}
	
	//Setters
	
	//Inner Classes
	private class MipmapData {
		
		private final ByteBuffer data;
		private final int rowBytes;
		
		public MipmapData(ByteBuffer data, int rowBytes) {
			this.data = data;
			this.rowBytes = rowBytes;
		}
		
		public ByteBuffer getDataBuffer() { return data; }
		public int getRowBytes() { return rowBytes; }
		public int getDataBytes() { return data.limit(); }
		
	}
	
}
