#ifndef _TEXTURE_INCLUDED_
#define _TEXTURE_INCLUDED_

#include <jni.h>

unsigned int loadKTXFile(JNIEnv *env, jobject assetManager, const char* file, GLenum target, int skipMipmaps);

#endif
