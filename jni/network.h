/**
 * network.h
 * Autor: Sascha Hlusiak
 *
 * Zentrale fuer Netzwerknachrichten des eigenen Protokolls
 **/

#ifndef __NETWORK_H_INCLUDED_
#define __NETWORK_H_INCLUDED_


#include "constants.h"

/* Der Port, der per Default benutzen soll */
const int TCP_PORT=59995;

/* Pseudotypen fuer vorzeichenbehaftete/vorzeichenlose 8bit/16bit Zahlen */
typedef signed short int16;
typedef signed char int8;
typedef unsigned short uint16;
typedef unsigned char uint8;

/* Die folgen Daten sollen nicht an Bytegrenzen ausgerichtet werden
   um Compiler- und Betriebssystemunabhängig zu sein. */
#pragma pack(1)

/* Header, stets der Kopf einer Netzwerknachricht.
   weitere Daten beinhalten als erstes Element immer einen NET_HEADER, damit die Pakete
   gecastet werden koennen, und man universell Zugriff auf den Header erhaelt. */
typedef struct
{
	uint8  check1;		/* Eine Checksumme um falsche Header zu erkennen */
	uint16 data_length;	/* Laenge des Datenpakets in Byte (network-byte-order) */
	uint8 msg_type;		/* Kennzahl der Nachricht, um den Typ festzustellen */
	uint8  check2;		/* Eine Checksumme um falsche Header zu erkennen */
} NET_HEADER,NET_START_GAME,NET_GAME_FINISH,NET_REQUEST_UNDO;

/**
 * Nachricht, um einen Spieler anzufordern. Enthält Wunschfarbe oder -1 für egal
 **/

typedef struct
{
	NET_HEADER header;
	/* added in 1.5 */
	int8 player;
	uint8 name[16];
} NET_REQUEST_PLAYER;

/**
 * NET_GRANT_PLAYER: Server gewaehrt dem Client einen Spieler
 * NET_CURRENT_PLAYER: Server hat aktuellen Spieler festgelegt
 **/

typedef struct
{
	NET_HEADER header;	/* Erstes Element ist stets NET_HEADER, damit gecastet werden kann */
	int8 player;		/* Spielernummer */
} NET_GRANT_PLAYER,NET_CURRENT_PLAYER,NET_REQUEST_HINT;

/**
 * Nachricht ueber die Aktion eines Steins (setzen oder Zuruecknahme)
 **/
typedef struct
{
	NET_HEADER header;
	int8 player;	/* Nummer des Spielers, der den Stein setzt */
	uint8 stone;	/* Nummer des Steins des Spielers */
	uint8 mirror_count,rotate_count;	/* Spiegelung und Rotation des Steins */
	int8 x,y;	/* Koordinaten des Steins, der aufs Spielfeld gesetzt wird */
} NET_SET_STONE,NET_UNDO_STONE;

/**
 * Serverstatus Paket, das der Server gelegentlich an die Clients schickt
 **/
typedef struct
{
	NET_HEADER header;
	int8 player,computer,clients; /* Anzahl menschlicher Spieler, Computerspieler und verbundener Clients */
	int8 width,height; /* Groesse des Spielfelds */
	int8 stone_numbers[STONE_SIZE_MAX]; /* Anzahl der Steine bestimmter Groessen */
	int8 gamemode;
	/* added in 1.5 */
	int8 spieler[PLAYER_MAX];
	uint8 client_names[CLIENTS_MAX][16]; /* names for each client */
} NET_SERVER_STATUS;

/**
 * Eine Chat-Nachricht mit Text. Verschickt vom Client an die Server und zurueck.
 **/
typedef struct
{
	NET_HEADER header;
	int8 client;	/* Server traegt hier die Nummer des Clients ein, der die Nachricht verschickt hat */
	uint8 length;	/* Textlaenge */
	uint8 text[1];	/* Array, das den Text beinhaltet. Ist NET_CHAT dynamisch und ausreichend
			   gross, kann problemlos auf hintere Teile des Arrays zurueckgegriffen werden */
} NET_CHAT;


/* Unbedingt Byte-Align wieder zuruecksetzen */
#pragma pack()

/* Nachrichtentyp-Konstanten der obigen Netzwerknachrichten. Ueber diese Konstanten werden die
   Pakete erst identifiziert. */
const int MSG_REQUEST_PLAYER=1;
const int MSG_GRANT_PLAYER=2;
const int MSG_CURRENT_PLAYER=3;
const int MSG_SET_STONE=4;
const int MSG_START_GAME=5;
const int MSG_GAME_FINISH=6;
const int MSG_SERVER_STATUS=7;
const int MSG_CHAT=8;
const int MSG_REQUEST_UNDO=9;
const int MSG_UNDO_STONE=10;
const int MSG_REQUEST_HINT=11;
const int MSG_STONE_HINT=12;


/* Bereitet die Netzwerknachricht *header vor und schickt sie an target.*/
int network_send(int target,NET_HEADER *header,uint16 data_length,uint8 msg_type);

/* Testet eine empfangene Nachricht auf (Un-)Gueltigkeit */
bool network_check_message(NET_HEADER *data);

/* Liest aus sock eine komplette Netzwerknachricht ein und speichert sie in *data */
const char* read_network_message(int sock,NET_HEADER *data,int size);


#ifndef WIN32
/* Unter Windows heissts closesocket(s), unter Linux hiermit auch, aber bedeutet close(s) */
int closesocket(int s);
#endif



#endif
