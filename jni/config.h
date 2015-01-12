#ifndef CONFIG_INCLUDED_
#define CONFIG_INCLUDED_


#define HAVE_GETADDRINFO 1

#define HAVE_LIBPTHREAD 1

#define HAVE_PTHREAD_CREATE 1

#define HAVE_SYS_SOCKET_H 1

#include <android/log.h>
#define  D(x...)  __android_log_print(ANDROID_LOG_INFO,"server",x)


#endif
