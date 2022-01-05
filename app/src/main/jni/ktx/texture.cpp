#include <stdlib.h>

#include "ktx.h"
#include "texture.h"

#include <jni.h>
#include <android/log.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>

#define LOG_TAG "ktx"

int loadKTXFile(JNIEnv *env, jobject assetManager, const char* file, GLenum target, int skipMipmaps) {
	GLboolean isMipmapped;
	GLenum glerror;
	KTX_dimensions dimensions;
	int ktxerror;

	AAssetManager* mgr = AAssetManager_fromJava(env, assetManager);
	AAsset* asset = AAssetManager_open(mgr, (const char *) file, AASSET_MODE_BUFFER);
	if (NULL == asset) {
		__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "ASSET %s NOT FOUND", file);
		return -1;
	}
	long size = AAsset_getLength(asset);
	const void* buffer = AAsset_getBuffer(asset);
	__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "loading texture %s", file);

	ktxerror = ktxLoadTextureM(buffer, size, target, &dimensions, &isMipmapped, skipMipmaps, &glerror, NULL, NULL);
	AAsset_close(asset);

	if (KTX_SUCCESS != ktxerror) {
		if (ktxerror == KTX_GL_ERROR)
			ktxerror = glerror;

		__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "ERROR loading %s texture", file);
		return ktxerror;
	}

	return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_de_saschahlusiak_freebloks_view_KTX_loadKTXTexture(JNIEnv *env, jobject thiz,
					jobject asset_manager, jstring file,
					jint target, jint skip_mipmaps) {
	const char *p = env->GetStringUTFChars(file, NULL);
	int ret;
	ret = loadKTXFile(env, asset_manager, p, target, skip_mipmaps);
	env->ReleaseStringUTFChars(file, p);
	return ret;
}
