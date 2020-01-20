/**
 * timer.cpp
 * Autor: Sascha Hlusiak
 *
 * Bietet gekapselten Zugriff auf genaue Zeitmessungen
 **/

#ifdef HAVE_CONFIG_H
  #include "config.h"
#endif

#ifdef WIN32
#include <windows.h>
#else
#include <sys/time.h>
#include <unistd.h>
#endif

#include "timer.h"

#ifdef WIN32
__int64 CTimer::frequency;
#endif

CTimer::CTimer()
{
#ifdef WIN32
	/* Unter Windows wird der PerformanceCounter benutzt.
	   Hier Frequenz davon anfordern und fuer spaeter merken. */
	QueryPerformanceFrequency((LARGE_INTEGER*)&frequency);
#endif
	/* Dann Zeitzaehler zuruecksetzen */
	reset();
}

/**
 * Gibt einen Zeitstempel zurueck, in Sekunden, aber bei sehr hoher Aufloesung
 * Der Zeitstempel ist relativ zu irgendeinem Wert, kann nur fuer Zeitdifferenzen gebraucht werden
 **/
const double CTimer::getTick()const
{
#ifdef WIN32
	__int64 tick;
	/* Zaehler des PerformanceCounters holen */
	QueryPerformanceCounter((LARGE_INTEGER*)&tick);
	/* PerformanceCounter/PerformanceCounterFrequency=Zeitstempel in sek */
	return (double)tick/(double)frequency;
#else
	timeval t1;
	/* Tageszeit holen */
	gettimeofday(&t1,0);
	/* gettimeofday hat eine Aufloesung im Mikrosekundenbereich
	   sek+usek/1000000=Zeitstempel in sek */
	return (double)(t1.tv_usec)/1000000.0+(double)(t1.tv_sec);
#endif
}

/**
 * Den aktuellen Zeitstempel merken
 */
void CTimer::reset()
{
	value=getTick();
}

/**
 * Vergangene Zeit ist die (aktuelle Zeit)-(vorher gemerkte Zeit)
 **/
const double CTimer::elapsed()const
{
	return getTick()-value;
}

/**
 * Funktion zum Schlafen von ms Millisekunden
 **/
void CTimer::sleep(const int ms)
{
#ifdef WIN32
	/* Windowseigene Funktion tut schon genau das */
	Sleep(ms);
#else
	/* Linux Funktion erwartet Mikrosekunden */
	usleep(ms*1000);
#endif
}


