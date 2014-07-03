LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_CPPFLAGS  := -DHAVE_CONFIG_H=1
LOCAL_MODULE    := server
LOCAL_SRC_FILES := spiel.cpp \
	stone.cpp \
	spielleiter.cpp \
	player.cpp \
	turn.cpp \
	turnpool.cpp \
	constants.cpp \
	spielserver.cpp \
	network.cpp \
	logger.cpp \
	timer.cpp \
	ki.cpp \
	de_saschahlusiak_freebloks_controller_JNIServer.cpp

LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)
