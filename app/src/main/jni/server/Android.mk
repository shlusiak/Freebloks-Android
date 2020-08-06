LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_CPPFLAGS  := -DHAVE_CONFIG_H=1
LOCAL_MODULE    := server
LOCAL_SRC_FILES := github/src/spiel.cpp \
	github/src/stone.cpp \
	github/src/spielleiter.cpp \
	github/src/player.cpp \
	github/src/turn.cpp \
	github/src/turnpool.cpp \
	github/src/spielserver.cpp \
	github/src/network.cpp \
	github/src/logger.cpp \
	github/src/timer.cpp \
	github/src/ki.cpp \
	github/src/constants.cpp \
	de_saschahlusiak_freebloks_controller_JNIServer.cpp

LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)
