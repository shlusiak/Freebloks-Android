/**
 * network.cpp
 * Autor: Sascha Hlusiak
 *
 * Funktionen fuer die Netzwerknachrichten des eigenen Protokolls
 **/

#ifdef HAVE_CONFIG_H
  #include "config.h"
#endif

#include <stdio.h>
#include <errno.h>
#include <string.h>

#ifdef WIN32
#include <winsock2.h>
#else
#include <unistd.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#endif

#include "network.h"

/**
 * Bereitet die Netzwerknachricht *header 
 * mit der Laenge data_length 
 * und dem Typ msg_type vor, 
 * und verschickt sie an target
 *
 * Rueckgabe: 0 bei Erfolg, -1 bei Fehler
 **/
int network_send(int target,NET_HEADER *header,uint16 data_length,uint8 msg_type)
{
	/* Setze wild aus vorhandenen Daten errechnete Checksums in der Datenstruktur */
	header->check1=(uint8)(data_length & 0x0055) ^ msg_type;
	/* Speichere data_length (in NETWORK BYTE ORDER) in der Struktur ab */
	header->data_length=htons(data_length);
	/* Nachrichtentyp in Nachricht speichern */
	header->msg_type=msg_type;
	/* Noch eine Checksum setzen errechnen. */
	header->check2=(uint8)(header->check1 ^ 0xD6)+msg_type;

#ifndef WIN32
	char c;
	/* Windows wuerde ggf. beim send() blockieren, wenn der Socket nicht mehr verbunden ist
	   dies mit einem recv() pruefen, und bei Fehler direkt raus. */
	if (recv(target,&c,1,MSG_PEEK|MSG_DONTWAIT)==0)return -1;
#endif
	/* Nachricht versenden */
	if (send(target,(char*)header,data_length,0)!=-1)return 0;
	perror("send: ");
	return -1;
}

/**
 * Eine Nachricht auf (Un-)Gueltigkeit ueberpruefen
 * Dazu werden einfach die Berechnungen (wie oben) nochmal gemacht
 * und mit den Checksums verglichen
 **/
bool network_check_message(NET_HEADER *data)
{
	/* Wenn Laenge des Datenpakets (angeblich) zu klein: Fehler! */
	if (ntohs(data->data_length)<sizeof(NET_HEADER))return false;
	/* Beiden Checksums erneut berechnen */
	uint8 c1=(uint8)(ntohs(data->data_length) & 0x0055) ^ data->msg_type;
	uint8 c2=(c1 ^ 0xD6)+data->msg_type;
	/* Bei Ungleichheit Fehler, sonst Nachricht ok */
	if (c1!=data->check1)return false;
	if (c2!=data->check2)return false;
	return true;
}

/**
 * Liest eine komplette Nachricht aus sock aus, und speichert sie in data
 *
 * Rueckgabe: 0 bei Erfolg
 *           (char*)(-1), wenn keine Nachricht vorliegt
 *           sonst Zeiger auf Zeichenkette mit Fehlermeldung.
 **/
const char* read_network_message(int sock,NET_HEADER *data,int size)
{
	static const char* S_INVALID_PACKAGE="Received invalid network message";

	/* Wenn garnicht verbunden, "liegt keine Nachricht vor" */
	if (sock==0)return (char*)(-1);
	errno=0;
	/* Genau den NET_HEADER einlesen */
	int r=recv(sock,(char*)data,sizeof(NET_HEADER),0);
	if (r==-1)
	{
		/* EAGAIN (und unter Windows <0>) heissen, dass kein Paket fuer das socket vorliegt */
		if (errno==EAGAIN)return (char*)(-1);
#ifdef WIN32
		if (errno==0)return (char*)(-1);
#endif
		/* Sonst ist ein Lesefehler aufgetreten. */
		return strerror(errno);
	}

	/* bei r==0 wurde der Socket ordnungsgemaess geschlossen. Die Verbindung zum Server
	   wurde verloren. */
	if (r==0)
	{	
// 		printf("Connection lost.\n");
		return "Connection reset by peer";
	}
	/* Wenn weniger Daten als ein NET_HEADER gelesen wurden, liegt ebenfalls ein Fehler vor */
	if ((unsigned int)r!=sizeof(NET_HEADER)){
// 		printf("Received too small package!\n");
		return S_INVALID_PACKAGE;
	}
	/* Nachricht auf halbwegs plausibel pruefen */
	if (!network_check_message(data)){
// 		printf("Received malformed package!\n");
		return S_INVALID_PACKAGE;
	}
	/* Nachricht scheint ok zu sein. Ermittele restliche Datenmenge */
	int restdaten=ntohs(data->data_length)-sizeof(NET_HEADER);
	
	/* Wenn keine Restdaten vorliegen, besteht die Nachricht nur aus einem NET_HEADER. Erfolg! */
	if (restdaten==0)return NULL;

	/* Wenn Datenpaket insgesamt die Groesse des Puffers ueberschreitet: Maechtig grosser FEHLER */
	if (ntohs(data->data_length)>size)
	{
// 		printf("Received too big package!\n");
		return S_INVALID_PACKAGE;
	}

	/* Sonst Restdaten einlesen und an Position hinter NET_HEADER in data schreiben */
	r=recv(sock,&((char*)(data))[sizeof(NET_HEADER)],restdaten,0);
	/* Wenn nicht genau so viele Daten gelesen werden konnten, wie in NET_HEADER suggeriert,
	   lag ein Paketfehler vor. */
	if (r!=restdaten)
	{
// 		printf("Package size mismatch!\n");
		return S_INVALID_PACKAGE;
	}
	/* ERFOLG! */
	return NULL;
}

#ifndef WIN32
/**
 * Die Funktion schliesst ein Socket, ist eigentlich nur da, um closesocket() auf close() zu mappen,
 * Da die Funktionen in Windoze und *NIX unterschiedlich heissen
 **/
int closesocket(int s)
{
	return close(s);
}
#endif

