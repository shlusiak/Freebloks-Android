/**
 * spielleiter.h
 * Autor: Sascha Hlusiak
 **/


#ifndef __SPIELLEITER_H_INCLUDED_
#define __SPIELLEITER_H_INCLUDED_

#include "spiel.h"
#include "constants.h"
#include "turnpool.h"
#include "turn.h"


#define PLAYER_COMPUTER (-2)	/* Spieler ist ein Computer */
#define PLAYER_LOCAL (-1)	/* Spieler ist ein lokaler Spieler */

enum GAMEMODE { GAMEMODE_2_COLORS_2_PLAYERS, GAMEMODE_4_COLORS_2_PLAYERS, GAMEMODE_4_COLORS_4_PLAYERS };

/**
 * Diese Klasse erweitert das CSpiel-Geruest um grundlegende Funktionen
 * eines Spielleiters.
 *
 * Von CSpielleiter werden CSpielServer und CSpielClient abgeleitet
 **/

class CSpielleiter:public CSpiel
{
protected:
	/* Nummer des aktuellen Spielers (beginnt bei 0), -1 ist niemand */
	int m_current_player;	
	/* Entweder PLAYER_COMPUTER, oder PLAYER_LOCAL.
	   Beim CSpielServer sind auch socket Handles moeglich, die einen Remote-Player
	   kennzeichnen	*/
	int spieler[PLAYER_MAX];

	/* Spielmodus */
	GAMEMODE m_gamemode;

	/* Die History aller Zuege, die somit auch zurueckgenommen werden koennen */
	CTurnpool *history;

	/* Fuegt einen Zug der History hinzu */
	void addHistory(CTurn *turn);
	void addHistory(int player,CStone *stone,int y,int x);


public:
	CSpielleiter();
	virtual ~CSpielleiter();

	void setSpieler(int i, int s) { spieler[i] = s; }
	void setCurrentPlayer(int c) { m_current_player = c; }

	/* Keinen Spieler als aktiv setzen */
	void set_noplayer() { m_current_player=-1; }
	/* Gibt die Nummer des aktuellen Spielers zurueck */
	const int current_player()const { return m_current_player; }

	/* Liefert den aktuellen Spieler als Objekt zurueck */
	CPlayer* get_current_player() { if (m_current_player==-1)return 0; else return get_player(m_current_player); }
	/* Gibt die Anzahl nicht-COMPUTER Spieler zurueck */
	const int num_players()const;

	/* Gibt den Spielmodus zurueck */
	const GAMEMODE get_gamemode()const { return m_gamemode; }
};

#endif
