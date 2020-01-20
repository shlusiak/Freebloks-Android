/**
 * timer.h
 * Autor: Sascha Hlusiak
 *
 * CTimer kapselt betriebssystemabhaengige Zeitfunktionen zu einer Schnittstelle
 **/

#ifndef _TIMER_H_INCLUDED_
#define _TIMER_H_INCLUDED_

#include <stdio.h>

/**
 * Kapselung fuer Zugriff auf genaue Zeitfunktionen
 **/
class CTimer
{
private:
#ifdef WIN32
	/* Unter Windows wird der PerformanceCounter verwendet. Hier wird die Frequenz davon gemerkt */
	static __int64 frequency;
#endif
	/* Ein relativer Zeit-Wert, gespeichert in Sekunden */
	double value;

	/* Gibt den aktuellen, nicht absoluten Zeitstempel zurueck, in Sekunden */
	const double getTick()const;
public:
	CTimer();

	/* Setzt den Timer zurueck. */
	void reset();

	/* Gibt Zeitdifferenz von jetzt bis zum vorherigem reset() zurueck, in Sekunden */
	const double elapsed()const;

	/* Gibt Zeitdifferenz vom letzten Checkpoint zurueck und setzt den Timer zurueck */
	const double checkPoint() { double bla=elapsed(); reset(); return bla; }

	/* Wartet die angegebene Anzahl von Millisekunden */
	static void sleep(const int ms);
};


/**
 * Baut CTimer zu einem simplen Zeitmesser zum Debuggen um
 **/
class CTimeCheck:public CTimer
{
private:
	const char *text;
public:
	/* Konstruktor merkt sich nur nen Text */
	CTimeCheck(const char* vtext):text(vtext) { }

	/* Destruktor gibt Text und die exakte Lebensdauer des Objekts aus. */
	~CTimeCheck() { printf("%s: %.5f sek.\n",text,elapsed()); }
};

#endif


