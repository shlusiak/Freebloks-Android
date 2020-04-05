#include <stdlib.h>

#include "ktx.h"
#include "texture.h"

#include <jni.h>
#include <android/log.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>

#define LOG_TAG "ktx"


unsigned int loadKTXFile(JNIEnv *env, jobject assetManager, const char* file, GLenum target, int skipMipmaps) {
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
Java_de_saschahlusiak_wordmix_game_view_BackgroundRenderer_loadKTXTexture(JNIEnv *env, jclass, jobject assetManager, jstring file, jint target, jint skipMipmaps) {
	const char *p = env->GetStringUTFChars(file, 0);
	int ret;
	ret = loadKTXFile(env, assetManager, p, target, skipMipmaps);
	env->ReleaseStringUTFChars(file, p);
	return ret;
}

