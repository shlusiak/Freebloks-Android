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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

final class DDSUtil {

	private DDSUtil() {		
	}
	
	public static void readFully(InputStream in, ByteBuffer out) throws IOException {
		out.mark();
		if (out.hasArray()) {
			while (out.hasRemaining()) {
				int r = in.read(out.array(), out.arrayOffset()+out.position(), out.remaining());
				if (r < 0) throw new EOFException();
				out.position(out.position() + r);
			}
		} else {
			byte[] temp = new byte[Math.min(out.remaining(), 8192)];
			while (out.hasRemaining()) {
				int r = in.read(temp, 0, Math.min(temp.length, out.remaining()));
				if (r < 0) throw new EOFException(""+out.position());
				out.put(temp, 0, r);
			}
		}
		out.reset();
	}
	
	public static int calculateMaskShift(int mask) {
		int leading = Integer.numberOfLeadingZeros(mask);
		int trailing = Integer.numberOfTrailingZeros(mask);
		int maskbits = (32 - leading - trailing);
		return trailing - 8 + maskbits;
	}
	
	public static int getCompressedBlockPixels(int fourCC) {
		return 4; //All compressed formats use 4x4 blocks
	}

	/**
	 * Returns the compressed block size in bytes for the specified compressed pixel format 
	 */
	public static int getCompressedBlockBytes(int fourCC) {
		switch (fourCC) {
		case DDSConstants.FOURCC_ETC:
		case DDSConstants.FOURCC_DXT1:
		case DDSConstants.FOURCC_BC1:
		case DDSConstants.FOURCC_BC4:
			return 8;
		}
		return 16;
	}
	
}
