LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_CFLAGS    := -DKTX_OPENGL_ES1=1
LOCAL_CPPFLAGS  := -DKTX_OPENGL_ES1=1

ifeq ($(TARGET_ARCH_ABI), x86)
    #LOCAL_CFLAGS += 
    #LOCAL_CPPFLAGS += 
endif

LOCAL_MODULE    := ktx
LOCAL_SRC_FILES := \
	loader.cxx \
	swap.cxx \
	errstr.cxx \
	checkheader.cxx \
	etcunpack.cxx \
	etcdec.cxx \
	texture.cpp

LOCAL_LDLIBS := -llog -lGLESv1_CM -landroid
include $(BUILD_SHARED_LIBRARY)

