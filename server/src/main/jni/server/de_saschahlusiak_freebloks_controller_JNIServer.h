/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class de_saschahlusiak_freebloks_controller_JNIServer */

#ifndef _Included_de_saschahlusiak_freebloks_controller_JNIServer
#define _Included_de_saschahlusiak_freebloks_controller_JNIServer
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL Java_de_saschahlusiak_freebloks_server_JNIServer_get_1number_1of_1processors
  (JNIEnv *, jobject thiz);

JNIEXPORT jint JNICALL Java_de_saschahlusiak_freebloks_server_JNIServer_native_1run_1server
  (JNIEnv *, jobject thiz, jstring, jint, jint, jint, jint, jintArray stones, jint, jint, jboolean);

JNIEXPORT jint JNICALL Java_de_saschahlusiak_freebloks_server_JNIServer_native_1resume_1server
  (JNIEnv *, jobject thiz, jstring, jint, jint, jint, jint, jintArray, jintArray, jintArray, jint, jint, jint, jboolean);

#ifdef __cplusplus
}
#endif
#endif
