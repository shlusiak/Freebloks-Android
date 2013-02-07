#ifdef HAVE_CONFIG_H
  #include "config.h"
#endif

#include "constants.h"
#include <stdio.h>
#include <stdlib.h>

void error_exit(const char* fehlertext, int fehlernummer){
	D("Fehler %d: %s\n", fehlernummer, fehlertext);
	getchar();
	exit (fehlernummer);
}
