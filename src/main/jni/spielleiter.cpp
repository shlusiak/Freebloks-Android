/**
 * spielleiter.cpp
 * Autor: Sascha Hlusiak
 *
 * Erweiterung von CSpiel um Grundlegende Funktionen eines Spielleiters
 **/

#ifdef HAVE_CONFIG_H
  #include "config.h"
#endif

#include "spielleiter.h"



CSpielleiter::CSpielleiter()
{
	m_current_player=-1;
	m_gamemode=GAMEMODE_4_COLORS_4_PLAYERS;
	for (int i=0;i<PLAYER_MAX;i++)spieler[i]=PLAYER_COMPUTER;
	history=new CTurnpool();
}

CSpielleiter::~CSpielleiter()
{
	if (history)delete history;
}

/**
 * Fuegt einen CTurn an die History hinten an. Dies ist der letzte Zug, der zurueckgenommen
 * werden kann.
 **/
void CSpielleiter::addHistory(CTurn *turn)
{
	history->add_turn(turn);
}

void CSpielleiter::addHistory(int player,CStone *stone,int y,int x)
{
	history->add_turn(player,stone,y,x);
}

/**
 * Gibt die Anzahl nicht-COMPUTER Spieler zurueck
 **/
const int CSpielleiter::num_players()const
{
	int n;
	n=0;
	for (int i=0;i<PLAYER_MAX;i++)if (spieler[i]!=PLAYER_COMPUTER)n++;
	return n;
}


