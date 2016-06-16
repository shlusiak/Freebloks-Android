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

final class DDSConstants {

	public static final int DDSD_CAPS        = 0x1;
	public static final int DDSD_HEIGHT      = 0x2;
	public static final int DDSD_WIDTH       = 0x4;
	public static final int DDSD_PITCH       = 0x8;
	public static final int DDSD_PIXELFORMAT = 0x1000;
	public static final int DDSD_MIPMAPCOUNT = 0x20000;
	public static final int DDSD_LINEARSIZE  = 0x80000;
	public static final int DDSD_DEPTH       = 0x800000;
	
	public static final int DDPF_FOURCC	= 0x4;
	
	public static final int FOURCC_DX10 = 0x30315844;
	public static final int FOURCC_DXT1 = 0x31545844;
	public static final int FOURCC_DXT3 = 0x33545844;
	public static final int FOURCC_DXT5 = 0x35545844;
	public static final int FOURCC_BC1  = 0x20314342;
	public static final int FOURCC_BC4  = 0x20344342;

	public static final int FOURCC_ATCI = 0x49435441;
	public static final int FOURCC_ATCA = 0x41435441;
	public static final int FOURCC_ETC  = 0x20435445;
	
	private DDSConstants() {		
	}
	
}
