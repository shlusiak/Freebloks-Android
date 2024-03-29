/* -*- tab-width: 4; -*- */
/* vi: set sw=2 ts=4: */

/**
 * @file
 * @~English
 *
 * @brief Functions for instantiating GL or GLES textures from KTX files.
 *
 * @author Georg Kolling, Imagination Technology
 * @author Mark Callow, HI Corporation
 *
 * $Revision: 21679 $
 * $Date:: 2013-05-22 19:03:13 +0900 #$
 */

/*
Copyright (c) 2010 The Khronos Group Inc.

Permission is hereby granted, free of charge, to any person obtaining a
copy of this software and/or associated documentation files (the
"Materials"), to deal in the Materials without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Materials, and to
permit persons to whom the Materials are furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be included
unaltered in all copies or substantial portions of the Materials.
Any additions, deletions, or changes to the original source files
must be clearly indicated in accompanying documentation.

If only executable code is distributed, then the accompanying
documentation must state that "this software is based in part on the
work of the Khronos Group".

THE MATERIALS ARE PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
MATERIALS OR THE USE OR OTHER DEALINGS IN THE MATERIALS.
*/

#include <string.h>
#include <stdlib.h>

#include "KHR/khrplatform.h"
#include "ktx.h"
#include "ktxint.h"

#include KTX_GLFUNCPTRS

/* @private is not preventing the typedefs, structs and defines from
 * appearing in the Doxygen output even though EXTRACT_PRIVATE is NO
 * in the config file. To prevent these items appearing I have changed
 * the special comments to ordinary comments, and have set
 * HIDE_UNDOC_MEMBERS = YES in the Doxygen config file.
 *
 * Items declared "static" are omitted, as expected, due to EXTRACT_STATIC
 * being NO, so there is no need to convert those to ordinary comments. 
 */
/*
 * @private
 * @~English
 * @brief type for a pointer to a stream reading function
 */
typedef int(*ktxStream_read)(void* dst, const GLsizei count, void* src);
/*
 * @private
 * @~English
 * @brief type for a pointer to a stream skipping function
 */
typedef int(*ktxStream_skip)(const GLsizei count, void* src);

/*
 * @private
 * @~English
 * @brief KTX stream interface
 */
struct ktxStream
{
	void* src;				/**< pointer to the stream source */
	ktxStream_read read;	/**< pointer to function for reading bytes */
	ktxStream_skip skip;	/**< pointer to function for skipping bytes */
};

/*
 * @private
 * @~English
 * @brief additional contextProfile bit indicating an OpenGL ES context.
 *
 * This is the same value NVIDIA returns when using an OpenGL ES profile
 * of their desktop drivers. However it is not specified in any official
 * specification as OpenGL ES does not support the GL_CONTEXT_PROFILE_MASK
 * query.
 */
#define _CONTEXT_ES_PROFILE_BIT 0x4

/*
 * @private
 * @~English
 * @name Supported Sized Format Macros
 *
 * These macros describe values that may be used with the sizedFormats
 * variable.
 */
/**@{*/
#define _NON_LEGACY_FORMATS 0x1 /*< @private @internal non-legacy sized formats are supported. */
#define _LEGACY_FORMATS 0x2  /*< @private @internal legacy sized formats are supported. */
/*
 * @private
 * @~English
 * @brief all sized formats are supported
 */
#define _ALL_SIZED_FORMATS (_NON_LEGACY_FORMATS | _LEGACY_FORMATS)
#define _NO_SIZED_FORMATS 0 /*< @private @internal no sized formats are supported. */
/**@}*/

/**
 * @private
 * @~English
 * @brief indicates the profile of the current context.
 */
static GLint contextProfile = 0;
/**
 * @private
 * @~English
 * @brief indicates what sized texture formats are supported
 *        by the current context.
 */
static GLint sizedFormats = _ALL_SIZED_FORMATS;
static GLboolean supportsSwizzle = GL_TRUE;
/**
 * @private
 * @~English
 * @brief indicates which R16 & RG16 formats are supported by the current context.
 */
static GLint R16Formats = _KTX_ALL_R16_FORMATS;
/**
 * @private
 * @~English
 * @brief indicates if the current context supports sRGB textures.
 */
static GLboolean supportsSRGB = GL_TRUE;


/**
 * @private
 * @~English
 * @brief Discover the capabilities of the current GL context.
 * 
 * Queries the context and sets several the following internal variables indicating
 * the capabilities of the context:
 *
 * @li sizedFormats
 * @li supportsSwizzle
 * @li supportsSRGB
 * @li b16Formats
 *
 */           
static void discoverContextCapabilities(void)
{
	GLint majorVersion = 1;
	GLint minorVersion = 0;

	if (strstr((const char*)glGetString(GL_VERSION), "GL ES") != NULL)
		contextProfile = _CONTEXT_ES_PROFILE_BIT;
	// MAJOR & MINOR only introduced in GL {,ES} 3.0
	glGetIntegerv(GL_MAJOR_VERSION, &majorVersion);
	glGetIntegerv(GL_MINOR_VERSION, &minorVersion);
	if (glGetError() != GL_NO_ERROR) {
		// < v3.0; resort to the old-fashioned way.
		if (contextProfile & _CONTEXT_ES_PROFILE_BIT)
			sscanf((const char*)glGetString(GL_VERSION), "OpenGL ES %d.%d ", &majorVersion, &minorVersion);
		else
			sscanf((const char*)glGetString(GL_VERSION), "OpenGL %d.%d ", &majorVersion, &minorVersion);
	}
	if (contextProfile & _CONTEXT_ES_PROFILE_BIT) {
		if (majorVersion < 3) {
			supportsSwizzle = GL_FALSE;
			sizedFormats = _NO_SIZED_FORMATS;
			R16Formats = _KTX_NO_R16_FORMATS;
			supportsSRGB = GL_FALSE;
		} else {
			sizedFormats = _NON_LEGACY_FORMATS;
		}
		if (strstr((const char*)glGetString(GL_EXTENSIONS), "GL_OES_required_internalformat") != NULL) {
			sizedFormats |= _ALL_SIZED_FORMATS;
		}
		// There are no OES extensions for sRGB textures or R16 formats.
	} else {
		// PROFILE_MASK was introduced in OpenGL 3.2.
		// Profiles: CONTEXT_CORE_PROFILE_BIT 0x1, CONTEXT_COMPATIBILITY_PROFILE_BIT 0x2.
		glGetIntegerv(GL_CONTEXT_PROFILE_MASK, &contextProfile);
		if (glGetError() == GL_NO_ERROR) {
			// >= 3.2
			if (majorVersion == 3 && minorVersion < 3)
				supportsSwizzle = GL_FALSE;
			if ((contextProfile & GL_CONTEXT_CORE_PROFILE_BIT))
				sizedFormats &= ~_LEGACY_FORMATS;
		} else {
			// < 3.2
			contextProfile = GL_CONTEXT_COMPATIBILITY_PROFILE_BIT;
			supportsSwizzle = GL_FALSE;
			// sRGB textures introduced in 2.0
			if (majorVersion < 2 && strstr((const char*)glGetString(GL_EXTENSIONS), "GL_EXT_texture_sRGB") == NULL) {
				supportsSRGB = GL_FALSE;
			}
			// R{,G]16 introduced in 3.0; R{,G}16_SNORM introduced in 3.1.
			if (majorVersion == 3) {
				if (minorVersion == 0)
					R16Formats &= ~_KTX_R16_FORMATS_SNORM;
			} else if (strstr((const char*)glGetString(GL_EXTENSIONS), "GL_ARB_texture_rg") != NULL) {
				R16Formats &= ~_KTX_R16_FORMATS_SNORM;
			} else {
				R16Formats = _KTX_NO_R16_FORMATS;
			}
		}
	}
}


/*
 * @~English
 * @brief Load a GL texture object from a ktxStream.
 *
 * This function will unpack compressed GL_ETC1_RGB8_OES and GL_ETC2_* format
 * textures in software when the format is not supported by the GL context,
 * provided the library has been compiled with SUPPORT_SOFTWARE_ETC_UNPACK
 * defined as 1.
 * 
 * It will also convert textures with legacy formats to their modern equivalents
 * when the format is not supported by the GL context, provided that the library
 * has been compiled with SUPPORT_LEGACY_FORMAT_CONVERSION defined as 1. 
 *
 * @param [in] stream		pointer to the ktxStream from which to load.
 * @param [in,out] pTexture	name of the GL texture to load. If NULL or if
 *                          <tt>*pTexture == 0</tt> the function will generate
 *                          a texture name. The function binds either the
 *                          generated name or the name given in @p *pTexture
 * 						    to the texture target returned in @p *pTarget,
 * 						    before loading the texture data. If @p pTexture
 *                          is not NULL and a name was generated, the generated
 *                          name will be returned in *pTexture.
 * @param [out] pTarget 	@p *pTarget is set to the texture target used. The
 * 						    target is chosen based on the file contents.
 * @param [out] pDimensions	If @p pDimensions is not NULL, the width, height and
 *							depth of the texture's base level are returned in the
 *                          fields of the KTX_dimensions structure to which it points.
 * @param [out] pIsMipmapped
 *	                        If @p pIsMipmapped is not NULL, @p *pIsMipmapped is set
 *                          to GL_TRUE if the KTX texture is mipmapped, GL_FALSE
 *                          otherwise.
 * @param [out] pGlerror    @p *pGlerror is set to the value returned by
 *                          glGetError when this function returns the error
 *                          KTX_GL_ERROR. glerror can be NULL.
 * @param [in,out] pKvdLen	If not NULL, @p *pKvdLen is set to the number of bytes
 *                          of key-value data pointed at by @p *ppKvd. Must not be
 *                          NULL, if @p ppKvd is not NULL.                     
 * @param [in,out] ppKvd	If not NULL, @p *ppKvd is set to the point to a block of
 *                          memory containing key-value data read from the file.
 *                          The application is responsible for freeing the memory.
 *
 *
 * @return	KTX_SUCCESS on success, other KTX_* enum values on error.
 *
 * @exception KTX_INVALID_VALUE @p target is @c NULL or the size of a mip
 * 							    level is greater than the size of the
 * 							    preceding level.
 * @exception KTX_INVALID_OPERATION @p ppKvd is not NULL but pKvdLen is NULL.
 * @exception KTX_UNEXPECTED_END_OF_FILE the file does not contain the
 * 										 expected amount of data.
 * @exception KTX_OUT_OF_MEMORY Sufficient memory could not be allocated to store
 *                              the requested key-value data.
 * @exception KTX_GL_ERROR      A GL error was raised by glBindTexture,
 * 								glGenTextures or gl*TexImage*. The GL error
 *                              will be returned in @p *glerror, if glerror
 *                              is not @c NULL.
 */
static
KTX_error_code
ktxLoadTextureS(struct ktxStream* stream, GLenum glTarget,
				KTX_dimensions* pDimensions, GLboolean* pIsMipmapped, int nSkipMipmaps,
				GLenum* pGlerror,
				unsigned int* pKvdLen, unsigned char** ppKvd)
{
	GLint				previousUnpackAlignment;
	KTX_header			header;
	KTX_texinfo			texinfo;
	void*				data = NULL;
	khronos_uint32_t	dataSize = 0;
	khronos_uint32_t    faceLodSize;
	khronos_uint32_t    faceLodSizeRounded;
	khronos_uint32_t	level;
	GLenum				glFormat, glInternalFormat;
	KTX_error_code		errorCode = KTX_SUCCESS;
	GLenum				errorTmp;

	if (pGlerror)
		*pGlerror = GL_NO_ERROR;

	if (ppKvd) {
		*ppKvd = NULL;
    }

	if (!stream || !stream->read || !stream->skip) {
		return KTX_INVALID_VALUE;
	}

	if (!stream->read(&header, KTX_HEADER_SIZE, stream->src)) {
		return KTX_UNEXPECTED_END_OF_FILE;
	}

	errorCode = _ktxCheckHeader(&header, &texinfo);
	if (errorCode != KTX_SUCCESS) {
		return errorCode;
	}


	if (ppKvd) {
		if (pKvdLen == NULL)
			return KTX_INVALID_OPERATION;
		*pKvdLen = header.bytesOfKeyValueData;
		if (*pKvdLen) {
			*ppKvd = (unsigned char*)malloc(*pKvdLen);
			if (*ppKvd == NULL)
				return KTX_OUT_OF_MEMORY;
			if (!stream->read(*ppKvd, *pKvdLen, stream->src))
			{
				free(*ppKvd);
				*ppKvd = NULL;

				return KTX_UNEXPECTED_END_OF_FILE;
			}
		}
	} else {
		/* skip key/value metadata */
		if (!stream->skip((long)header.bytesOfKeyValueData, stream->src)) {
			return KTX_UNEXPECTED_END_OF_FILE;
		}
	}

	if (contextProfile == 0)
		discoverContextCapabilities();

	/* KTX files require an unpack alignment of 4 */
	glGetIntegerv(GL_UNPACK_ALIGNMENT, &previousUnpackAlignment);
	if (previousUnpackAlignment != KTX_GL_UNPACK_ALIGNMENT) {
		glPixelStorei(GL_UNPACK_ALIGNMENT, KTX_GL_UNPACK_ALIGNMENT);
	}

	glInternalFormat = header.glInternalFormat;
	glFormat = header.glFormat;
	if (!texinfo.compressed) {
		// With only unsized formats must change internal format.
		if (sizedFormats == _NO_SIZED_FORMATS
			|| (!(sizedFormats & _LEGACY_FORMATS) &&
				(header.glBaseInternalFormat == GL_ALPHA	
				|| header.glBaseInternalFormat == GL_LUMINANCE
				|| header.glBaseInternalFormat == GL_LUMINANCE_ALPHA
				|| header.glBaseInternalFormat == GL_INTENSITY))) {
			glInternalFormat = header.glBaseInternalFormat;
		}
	}

	if (nSkipMipmaps >= header.numberOfMipmapLevels)
		nSkipMipmaps = header.numberOfMipmapLevels - 1;
	if (nSkipMipmaps < 0)
		nSkipMipmaps = 0;

	for (level = 0; level < header.numberOfMipmapLevels; ++level)
	{
		GLsizei pixelWidth  = MAX(1, header.pixelWidth  >> level);
		GLsizei pixelHeight = MAX(1, header.pixelHeight >> level);
		GLsizei pixelDepth  = MAX(1, header.pixelDepth  >> level);

		if (!stream->read(&faceLodSize, sizeof(khronos_uint32_t), stream->src)) {
			errorCode = KTX_UNEXPECTED_END_OF_FILE;
			goto cleanup;
		}

		if (header.endianness == KTX_ENDIAN_REF_REV) {
			_ktxSwapEndian32(&faceLodSize, 1);
		}
		faceLodSizeRounded = (faceLodSize + 3) & ~(khronos_uint32_t)3;
		if (!data) {
			/* allocate memory sufficient for the first level */
			data = malloc(faceLodSizeRounded);
			if (!data) {
				errorCode = KTX_OUT_OF_MEMORY;
				goto cleanup;
			}
			dataSize = faceLodSizeRounded;
		}
		else if (dataSize < faceLodSizeRounded) {
			/* subsequent levels cannot be larger than the first level */
			errorCode = KTX_INVALID_VALUE;
			goto cleanup;
		}

		if (!stream->read(data, faceLodSizeRounded, stream->src)) {
			errorCode = KTX_UNEXPECTED_END_OF_FILE;
			goto cleanup;
		}

		if (level < nSkipMipmaps)
			continue;

		/* Perform endianness conversion on texture data */
		if (header.endianness == KTX_ENDIAN_REF_REV && header.glTypeSize == 2) {
			_ktxSwapEndian16((khronos_uint16_t*)data, faceLodSize / 2);
		}
		else if (header.endianness == KTX_ENDIAN_REF_REV && header.glTypeSize == 4) {
			_ktxSwapEndian32((khronos_uint32_t*)data, faceLodSize / 4);
		}

		if (texinfo.textureDimensions == 1) {
			if (texinfo.compressed) {
				glCompressedTexImage1D(glTarget, level - nSkipMipmaps,
					glInternalFormat, pixelWidth, 0,
					faceLodSize, data);
			} else {
				glTexImage1D(glTarget, level - nSkipMipmaps,
					glInternalFormat, pixelWidth, 0,
					glFormat, header.glType, data);
			}
		} else if (texinfo.textureDimensions == 2) {
			if (header.numberOfArrayElements) {
				pixelHeight = header.numberOfArrayElements;
			}
			if (texinfo.compressed) {
				// It is simpler to just attempt to load the format, rather than divine which
				// formats are supported by the implementation. In the event of an error,
				// software unpacking can be attempted.
				glCompressedTexImage2D(glTarget, level - nSkipMipmaps,
					glInternalFormat, pixelWidth, pixelHeight, 0,
					faceLodSize, data);
			} else {
				glTexImage2D(glTarget, level - nSkipMipmaps,
					glInternalFormat, pixelWidth, pixelHeight, 0,
					glFormat, header.glType, data);
			}
		} else if (texinfo.textureDimensions == 3) {
			if (header.numberOfArrayElements) {
				pixelDepth = header.numberOfArrayElements;
			}
			if (texinfo.compressed) {
				glCompressedTexImage3D(glTarget, level - nSkipMipmaps,
					glInternalFormat, pixelWidth, pixelHeight, pixelDepth, 0,
					faceLodSize, data);
			} else {
				glTexImage3D(glTarget, level - nSkipMipmaps,
					glInternalFormat, pixelWidth, pixelHeight, pixelDepth, 0,
					glFormat, header.glType, data);
			}
		}

		errorTmp = glGetError();
#if SUPPORT_SOFTWARE_ETC_UNPACK
		// Renderion is returning INVALID_VALUE. Oops!!
		if ((errorTmp == GL_INVALID_ENUM || errorTmp == GL_INVALID_VALUE)
			&& texinfo.compressed
			&& texinfo.textureDimensions == 2
			&& (glInternalFormat == GL_ETC1_RGB8_OES || (glInternalFormat >= GL_COMPRESSED_R11_EAC && glInternalFormat <= GL_COMPRESSED_SRGB8_ALPHA8_ETC2_EAC)))
			{
			GLubyte* unpacked;
			GLenum format, internalFormat, type;

			errorCode = _ktxUnpackETC((GLubyte*)data, glInternalFormat, pixelWidth, pixelHeight,
									  &unpacked, &format, &internalFormat, &type,
									  R16Formats, supportsSRGB);
			if (errorCode != KTX_SUCCESS) {
				goto cleanup;
			}
//			if (!(sizedFormats & _NON_LEGACY_FORMATS)) {
				if (internalFormat == GL_RGB8)
					internalFormat = GL_RGB;
				else if (internalFormat == GL_RGBA8)
					internalFormat = GL_RGBA;
//			}
			glTexImage2D(glTarget, level - nSkipMipmaps,
						 internalFormat, pixelWidth, pixelHeight, 0,
						 format, type, unpacked);

			free(unpacked);
			errorTmp = glGetError();
		}
#endif
		if (errorTmp != GL_NO_ERROR) {
			if (pGlerror)
				*pGlerror = errorTmp;
			errorCode = KTX_GL_ERROR;
			goto cleanup;
		}
	}

cleanup:
	free(data);

	/* restore previous GL state */
	if (previousUnpackAlignment != KTX_GL_UNPACK_ALIGNMENT) {
		glPixelStorei(GL_UNPACK_ALIGNMENT, previousUnpackAlignment);
	}

	if (errorCode == KTX_SUCCESS)
	{
		if (pDimensions) {
			pDimensions->width = header.pixelWidth;
			pDimensions->height = header.pixelHeight;
			pDimensions->depth = header.pixelDepth;
		}
		if (pIsMipmapped) {
			if (header.numberOfMipmapLevels - nSkipMipmaps > 1)
				*pIsMipmapped = GL_TRUE;
			else
				*pIsMipmapped = GL_FALSE;
		}
	} else {
		if (ppKvd && *ppKvd)
		{
			free(*ppKvd);
			*ppKvd = NULL;
		}
	}
	return errorCode;
}


/* Implementation of ktxStream for memory */

struct ktxMem
{
	const unsigned char* bytes;
	GLsizei size;
	GLsizei pos;
};

static
int ktxMemStream_read(void* dst, const GLsizei count, void* src)
{
	struct ktxMem* mem = (struct ktxMem*)src;
	
	if(!dst || !mem || (mem->pos + count > mem->size) || (mem->pos + count < mem->pos))
	{
		return 0;
	}

	memcpy(dst, mem->bytes + mem->pos, count);
	mem->pos += count;

	return 1;
}

static
int ktxMemStream_skip(const GLsizei count, void* src)
{
	struct ktxMem* mem = (struct ktxMem*)src;

	if(!mem || (mem->pos + count > mem->size) || (mem->pos + count < mem->pos))
	{
		return 0;
	}

	mem->pos += count;

	return 1;
}

static
int ktxMemInit(struct ktxStream* stream, struct ktxMem* mem, const void* bytes, GLsizei size)
{
	if (!stream || !mem || !bytes || (size <= 0))
	{
		return 0;
	}
	
	mem->bytes = (const unsigned char*)bytes;
	mem->size = size;
	mem->pos = 0;

	stream->src = mem;
	stream->read = ktxMemStream_read;
	stream->skip = ktxMemStream_skip;

	return 1;
}

/**
 * @~English
 * @brief Load a GL texture object from KTX formatted data in memory.
 *
 * @param [in] bytes		pointer to the array of bytes containing
 * 							the KTX format data to load.
 * @param [in] size			size of the memory array containing the
 *                          KTX format data.
 * @param [in,out] pTexture	name of the GL texture to load. See
 *                          ktxLoadTextureF() for details.
 * @param [out] pTarget 	@p *pTarget is set to the texture target used. See
 *                          ktxLoadTextureF() for details.
 * @param [out] pDimensions @p the texture's base level width depth and height
 *                          are returned in structure to which this points.
 *                          See ktxLoadTextureF() for details.
 * @param [out] pIsMipmapped @p *pIsMipMapped is set to indicate if the loaded
 *                          texture is mipmapped. See ktxLoadTextureF() for
 *                          details.
 * @param [out] pGlerror    @p *pGlerror is set to the value returned by
 *                          glGetError when this function returns the error
 *                          KTX_GL_ERROR. glerror can be NULL.
 * @param [in,out] pKvdLen	If not NULL, @p *pKvdLen is set to the number of bytes
 *                          of key-value data pointed at by @p *ppKvd. Must not be
 *                          NULL, if @p ppKvd is not NULL.                     
 * @param [in,out] ppKvd	If not NULL, @p *ppKvd is set to the point to a block of
 *                          memory containing key-value data read from the file.
 *                          The application is responsible for freeing the memory.*
 *
 * @return	KTX_SUCCESS on success, other KTX_* enum values on error.
 *
 * @exception KTX_FILE_OPEN_FAILED	The specified memory could not be opened as a file.
 * @exception KTX_INVALID_VALUE		See ktxLoadTextureF() for causes.
 * @exception KTX_INVALID_OPERATION	See ktxLoadTextureF() for causes.
 * @exception KTX_UNEXPECTED_END_OF_FILE See ktxLoadTextureF() for causes.
 * 								
 * @exception KTX_GL_ERROR			See ktxLoadTextureF() for causes.
 */
KTX_error_code
ktxLoadTextureM(const void* bytes, GLsizei size, GLenum glTarget,
				KTX_dimensions* pDimensions, GLboolean* pIsMipmapped, int nSkipMipmaps,
				GLenum* pGlerror,
				unsigned int* pKvdLen, unsigned char** ppKvd)
{
	struct ktxMem mem;
	struct ktxStream stream;

	if (!ktxMemInit(&stream, &mem, bytes, size))
	{
		return KTX_FILE_OPEN_FAILED;
	}

	return ktxLoadTextureS(&stream, glTarget, pDimensions, pIsMipmapped, nSkipMipmaps, pGlerror, pKvdLen, ppKvd);
}


