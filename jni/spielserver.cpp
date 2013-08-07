/**
 * spielserver.cpp
 * Autor: Sascha Hlusiak
 *
 * CSpielServer: Server fuer ein Spiel
 * CSpielListener: Akzeptiert Verbindungen an einem Netzwerksocket und errichtet einen CSpielServer
 **/

#ifdef HAVE_CONFIG_H
  #include "config.h"
#endif

#include <stdio.h>
#include <stdlib.h>
#include <errno.h>

#ifdef WIN32
#include <winsock2.h>
#include <ws2tcpip.h>
#define snprintf _snprintf
#else
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/select.h>
#include <sys/un.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <fcntl.h>
#include <pthread.h>
#endif

#include "spielserver.h"
#include "timer.h"


/**
 * Construktor: Leeren SpielServer erstellen, alles auf Standardeinstellung
 **/
CSpielServer::CSpielServer(const int v_max_humans,const int v_ki_mode,const GAMEMODE v_gamemode,int v_forceDelay)
:ki_mode(v_ki_mode),max_humans(v_max_humans),forceDelay(v_forceDelay)
{
	int i;
	start_new_game(v_gamemode);
	for (i=0;i<CLIENTS_MAX;i++) {
		clients[i]=0;
		names[i] = NULL;
	}
	m_gamemode=v_gamemode;
	if (m_gamemode==GAMEMODE_4_COLORS_2_PLAYERS)
		set_teams(0,2,1,3);
	/* Per Default mit 1/1/1/1/1 Steinen starten. Wird bei Bedarf anders aufgerufen */
	set_stone_numbers(1,1,1,1,1);
	logger=NULL;
}

/**
 * Destruktor: Verbindung zu allen Clients beenden
 **/
CSpielServer::~CSpielServer()
{
	int i;
	for (i=0;i<CLIENTS_MAX;i++) {
		if (clients[i]!=0)
			closesocket(clients[i]);
		if (names[i])
			free(names[i]);
		names[i] = NULL;
	}
}

/**
 * Gibt Anzahl der Verbundenen Clients zurueck
 **/
int CSpielServer::num_clients()const
{
	int n;
	n=0;
	for (int i=0;i<CLIENTS_MAX;i++)if (clients[i]!=0)n++;
	return n;
}

/**
 * Einen Client der Liste bekannter Clients hinzufuegen
 * Ist kein Platz mehr frei (sind CLIENTS_MAX Clients verbunden),
 * wird die Verbindung einfach getrennt
 **/
void CSpielServer::add_client(int s)
{
	for (int i=0;i<CLIENTS_MAX;i++)if (clients[i]==0)
	{
		char c[50];
		/* Alle Clients ueber neuen Spieler informieren. */
		sprintf(c,"Client %d joined",i);
		if (logger)
		{
			logger->logLine(c);
		}
		send_server_msg(c);

		/* Freien Platz gefunden, Client speichern */
		clients[i]=s;
		/* Und aktuellen Serverstatus verbreiten */
		send_server_status();
		return;
	}
	/* Sonst Verbindung schliessen */
	closesocket(s);
}

/**
 * Entfernt den client[index], schliesst seine TCP Verbindung und ersetzt ihn durch einen COMPUTER
 **/

void CSpielServer::delete_client(int index,bool notify)
{
	/* Alle Spieler, die der Client belegt hat, werden durch einen PLAYER_COMPUTER ersetzt */
	for (int i=0;i<PLAYER_MAX;i++)if (spieler[i]==clients[index]) {
		spieler[i]=PLAYER_COMPUTER;
	}
	/* Socket zu dem Client schliessen */
	if (closesocket(clients[index])==-1)perror("close: ");
	clients[index]=0;
	/* Aktuellen Serverstatus an restliche Clients verbreiten */
	send_server_status();
	if (notify)
	{
		char c[256];
		sprintf(c,"Client %d left\n",index);
		if (logger)
		{
			logger->logLine(c);
		}
		send_server_msg(c);
	}
}

/**
 * Hauptschleife des SpielServers.
 * Wird aufgerufen, sobald das Spiel laeuft, und hier wird es zuende gefuehrt.
 * Es werden abwechselnd Netzwerknachrichten der Clients verarbeitet, sowie die Zuege der KI
 * berechnet.
 **/
void CSpielServer::run()
{
    /* filedescriptors erhaelt alle sockets, und ein select() wartet dann auf eine Netzwerknachricht
       irgendeines Clients */
    fd_set filedescriptors;

    /* Fuer timeout bei select() */
    timeval tv;
    int retval;
    int max;
    int heartbeat;
	CTimer timer;

    heartbeat = 0;

    do
    {
	/* Wenn das Spiel laeuft, und der aktuelle Spieler ein Computerspieler ist,
	   berechne Zug der KI */
	if (m_current_player!=-1 && spieler[m_current_player]==PLAYER_COMPUTER)
	{
		/* Ermittle CTurn, den die KI jetzt setzen wuerde */
		timer.reset();
		CTurn *turn=m_ki.get_ki_turn(this, current_player(),ki_mode);
		if (forceDelay && timer.elapsed() < 800)
			timer.sleep(800 - timer.elapsed());

		if (turn!=0)
		{
			/* Datenstruktur fuellen, die an die Clients geschickt werden soll. */
			NET_SET_STONE data;

			data.player=current_player();
			data.stone=turn->get_stone_number();
			data.mirror_count=turn->get_mirror_count();
			data.rotate_count=turn->get_rotate_count();
			data.x=turn->get_x();
			data.y=turn->get_y();
			/* Zug lokal wirklich setzen, wenn Fehlschlag, ist das Spiel wohl nicht mehr synchron */
			if ((CSpiel::is_valid_turn(turn) == FIELD_DENIED) ||
			   (CSpiel::set_stone(turn)!=FIELD_ALLOWED))
			{
				printf("Game not in sync (2)\n");
				exit(2);
			}
			/* sonst Spielzug an alle Clients uebermitteln */
			send_all((NET_HEADER*)&data,sizeof(data),MSG_SET_STONE);
			/* Sowie den Zug der History anhaengen */
			addHistory(turn);
		}
		/* Naechsten Spieler ermitteln */
		next_player();

		/* Ausgewaehlten aktuellen Spieler an alle Clients schicken */
		send_current_player();
	} else
		heartbeat++;

	if (heartbeat > 60) {
		/* send out a server status message. this should result in
		   timeouts, if the client disconnected and there is no data
		   flowing.

		   in worst case, heartbeats will be sent about every 30 sec */
		heartbeat = 0;
		send_server_status();
	}


	if (m_current_player!=-1 && spieler[current_player()]==PLAYER_COMPUTER)
	{
		/* Ist nun ein Computer an der Reihe, kein Timeout von 100 ms */
		tv.tv_sec=0;
		tv.tv_usec=0;
	}else{
		/* Bei menschlichen Spielern benutze Timeout von 1s */
		tv.tv_sec = 0;
		tv.tv_usec = 500*1000;
	}

	/* In Schleife alle anliegenden Nachrichten verarbeiten */
	do
	{	/* Leere Array der filedescriptors */
		FD_ZERO(&filedescriptors);
		/* Und fuelle es mit den sockets der Clients */
		for (int i=max=0;i<CLIENTS_MAX;i++)if (clients[i]!=0)
		{
			FD_SET((unsigned int)clients[i],&filedescriptors);
			if (max<clients[i])max=clients[i];
		}
		/* Select blockiert, bis der timeout abgelaufen ist, oder Daten von einem der Sockets anliegen */

		retval = select(max+1, &filedescriptors, NULL, NULL, &tv);
		if (retval>0)
		{
			/* Es liegen Daten eines Sockets vor, verarbeite alle Nachrichten aller betroffenen Clients */
			for (int i=0;i<CLIENTS_MAX;i++)
				if (clients[i]!=0 && FD_ISSET(clients[i],&filedescriptors))
			{
				/* Bestimmten Client i abarbeiten */
	 			handle_client(i);
			}
		}
	}while (retval>0);

    /* Der Server laeuft in der Schleife, solange noch mindestens ein Client verbunden ist. 
       Sind alle Clients getrennt, wird der Server hier beendet. */
    }while (num_clients()>0);
}

/**
 * Verarbeitet eine einzige Nachricht eines bestimmten Clients, von dem Daten anliegen
 */
void CSpielServer::handle_client(int index)
{
	const char *err;
	char buffer[512];
	/* Lese genau eine Netzwerknachricht des Clients in buffer ein. */
	err=read_network_message(clients[index],(NET_HEADER*)buffer,sizeof(buffer));
	/* 0 heisst Erfolg, ansonsten ist ein Fehler aufgetreten. */
	if (err==NULL)process_message(index,(NET_HEADER*)buffer);
	/* -1 hiesse, es liegen keine Daten vor, was hier nicht auftreten darf
	   (da lt. select() Daten anliegen) */
	else {
		/* Bei Fehler beende Verbindung zum Client */
		delete_client(index,true);
		return;
	}
}

/**
 * Verarbeitet eine von einem Client empfangene Netzwerknachricht
 **/
void CSpielServer::process_message(int client,NET_HEADER* data)
{
	switch(data->msg_type)
	{
		/* Der Client fordert einen lokalen Spieler an */
		case MSG_REQUEST_PLAYER: {
			NET_REQUEST_PLAYER *req = (NET_REQUEST_PLAYER*)(data);
			NET_GRANT_PLAYER msg;
			int n;
			/* Sind nicht mehr Menschen erlaubt, verwirf die Nachricht */
			if (num_players()>=max_humans)return;

			/* Ebenso, wenn das Spiel bereits laeuft */
			if (m_current_player!=-1) return;

			if (m_gamemode==GAMEMODE_2_COLORS_2_PLAYERS || m_gamemode==GAMEMODE_DUO)
			{
				/* Wenn bereits zwei Spieler drin sind, raus */
				if (num_players()>=2)return;
				/* Pick zufaellig einen Spieler raus */
				n=(rand()%2)*2;
				/* seit 1.5: optional ist ein Wunschspieler angegeben */
				if (ntohs(data->data_length) > sizeof(NET_HEADER)) {
					if (req->player == 0 || req->player == 2)
						n = req->player;
				}
				/* Suche den naechsten, der frei ist (!=PLAYER_COMPUTER) */
				n = 2 - n;
				if (spieler[n] != PLAYER_COMPUTER)
					return;
			}else{
				/* Wenn alle Spieler vergeben, raus */
				if (num_players()>=PLAYER_MAX)return;
				/* Pick zufaellig einen Spieler raus */
				n=rand()%PLAYER_MAX;
				/* seit 1.5: optional ist ein Wunschspieler angegeben */
				if (ntohs(data->data_length) > sizeof(NET_HEADER)) {
					if (req->player >= 0 && req->player <= PLAYER_MAX)
						n = req->player;
				}
				/* Suche den naechsten, der frei ist (!=PLAYER_COMPUTER) */
				while (spieler[n]!=PLAYER_COMPUTER)n=(n+1)%PLAYER_MAX;
			}

			/* Schick eine Nachricht zurueck, der ihm den Spieler zugesteht */
			msg.player=n;
			network_send(clients[client],(NET_HEADER*)&msg,sizeof(msg),MSG_GRANT_PLAYER);
			/* Speichere socket des Spielers in dem spieler[] Array
			   So werden den Spielern wieder die Clients zugeordnet */
			spieler[n]=clients[client];

			if (m_gamemode==GAMEMODE_4_COLORS_2_PLAYERS)
			{
				/* Jeder Spieler kriegt 2 Farben, also gib ihm seine zweite Farbe dazu */
				n=(n+2)%PLAYER_MAX;
				if (spieler[n]!=PLAYER_COMPUTER)
				{
					printf("ERROR: Spieler bereits vergeben!?!?!? (2 Farben pro Spieler)\n");
					delete_client(client,false);
					return;
				}
				msg.player=n;
				network_send(clients[client],(NET_HEADER*)&msg,sizeof(msg),MSG_GRANT_PLAYER);
				/* Speichere socket des Spielers in dem spieler[] Array
			   	So werden den Spielern wieder die Clients zugeordnet */
				spieler[n]=clients[client];

			}
			/* setze Spielernamen */
			if (names[client])
				free(names[client]);
			names[client] = NULL;
			if (ntohs(data->data_length) > sizeof(NET_HEADER)) {
				req->name[sizeof(req->name) - 1] = '\0';
				if (strlen((char*)req->name) > 0) {
					/* store client name */
					names[client] = strdup((char*)req->name);
				}
			}

			/* Aktuellen Serverstatus an Clients senden */
			send_server_status();
// 			printf("Client %d requested player (#%d)\n",client,n);
			break;
		}

		/* Ein Client hat einen Stein gesetzt */
		case MSG_SET_STONE:{
			NET_SET_STONE *s=(NET_SET_STONE*)data;
			/* Den entsprechenden Stein aus den Daten zusammensuchen */
			CStone *stone=get_player(s->player)->get_stone(s->stone);
			stone->mirror_rotate_to(s->mirror_count,s->rotate_count);

			/* Den Stein lokal setzen */
 			if ((CSpiel::is_valid_turn(stone, s->player, s->y, s->x) == FIELD_DENIED) ||
			   (CSpiel::set_stone(stone, s->player,s->y,s->x)==FIELD_ALLOWED))
			{
				/* Bei Erfolg wird die Nachricht direkt an alle Clients zurueck-
				   geschickt */
				send_all(data,ntohs(data->data_length),MSG_SET_STONE);
				/* Zug an History anhaengen */
				addHistory(s->player,stone,s->y,s->x);
				/* Dann wird der naechste Spieler ermittelt*/
				next_player();
			}else{ // Spiel scheint nicht synchron zu sein
				if (logger) {
					logger->logLine("Game seems to be out of sync.\n");
				}
			}
			/* Aktuellen Spieler den Clients mitteilen */
			send_current_player();
			break;
		}

		/* Ein Client erbittet Spielstart. So sei es! */
		case MSG_START_GAME:
// 			printf("Request game start\n");
			start_game();
			break;

		/* Eine Chat-Nachricht von einem Client empfangen. */
		case MSG_CHAT:
			/* Setze in der Nachricht die Nummer des Clients, der sie versendet hat. */
			((NET_CHAT*)data)->client=client;

			/* Zwangsnullterminiere den empfangenen Text. Nur zur Sicherheit. */
			((NET_CHAT*)data)->text[ntohs(data->data_length)-sizeof(NET_CHAT)-1]='\0';

			/* Schicke leicht modifizierte Chat-Nachricht an alle anderen Clients weiter. */
			send_all(data,ntohs(data->data_length),MSG_CHAT);

			if (logger)
			{
				logger->logLine("Client %d: %s\n",client,((NET_CHAT*)data)->text);
			}
			break;

		/* Ein Client will eine Zugzuruecknahme */
		case MSG_REQUEST_UNDO: {
			/* Zugzuruecknahme ist nur bei einem Client oder einem Menschlichem 
			   Spieler zulaessig. */
			if (num_clients()>1 && num_players()>1)
			{
// 				printf("Client %d requested undo. IGNORED.\n",client);
				return;
			}
// 			printf("Client %d requesting undo. ",client);
			NET_UNDO_STONE undo;
			int i=0;
			/* Solange Steine zurueck nehmen, bis keine mehr in der History vorliegen,
			   oder ein menschlicher Spieler wieder dran ist. */
			do
			{
				CTurn *turn=history->get_last_turn();
				if (turn==NULL)break; // Kein Zug mehr in der History
				i++;
				// "Zug zuruecknehmen" an Clients senden
				send_all((NET_HEADER*)(&undo),sizeof(undo),MSG_UNDO_STONE);
				// Spieler von zurueckgenommenen Stein ist wieder dran
				m_current_player=turn->get_playernumber();
				// Aktuellen Spieler uebermitteln
				send_current_player();
				// Und lokal den Zug zuruecknehmen
				undo_turn(history, m_gamemode);
				// Solange Zuege des Computers zurueckgenommen werden
			}while (spieler[m_current_player]==PLAYER_COMPUTER);
// 			printf("Removed %d turns.\n",i);
			break;
		}
		case MSG_REQUEST_HINT: {
			CTurn *turn=m_ki.get_ki_turn(this,((NET_REQUEST_HINT*)data)->player,KI_HARD);
			NET_SET_STONE d;

			d.player=((NET_REQUEST_HINT*)data)->player;
			d.stone=turn->get_stone_number();
			d.mirror_count=turn->get_mirror_count();
			d.rotate_count=turn->get_rotate_count();
			d.x=turn->get_x();
			d.y=turn->get_y();
			network_send(clients[client],(NET_HEADER*)&d,sizeof(d),MSG_STONE_HINT);

			break;
		}

		default: printf("FEHLER: Unbehandelte Netzwerknachricht: #%d\n",data->msg_type);
			break;
	}
}

/**
 * Schicke eine Nachricht an alle verbundenen Clients
 **/
void CSpielServer::send_all(NET_HEADER* data,uint16 data_length,uint8 msg_type)
{
	for (int i=0;i<CLIENTS_MAX;i++)if (clients[i]!=0)
		if (network_send(clients[i],data,data_length,msg_type)==-1)
	{
		/* Fehler beim Senden, trenne Verbindung zum Client. */
		delete_client(i,true);
	}
}

/**
 * Schicke aktuelle Spielernummer an alle Clients
 **/
void CSpielServer::send_current_player()
{
	NET_CURRENT_PLAYER data;
	data.player=current_player();
	send_all((NET_HEADER*)&data,sizeof(data),MSG_CURRENT_PLAYER);
}

/**
 * Schicke Serverstatus an alle Clients
 * (Anzahl Spieler, Anzahl Computerspieler, Anzahl verbundener Clients, Breite und Hoehe)
 **/
void CSpielServer::send_server_status()
{
	NET_SERVER_STATUS status;
	int i;
	int max=(m_gamemode==GAMEMODE_2_COLORS_2_PLAYERS || m_gamemode==GAMEMODE_DUO)?2:PLAYER_MAX;
	status.player=num_players();
	status.computer=max-num_players();
	status.clients=num_clients();
	status.width=get_field_size_x();
	status.height=get_field_size_y();
	for (i=0;i<STONE_SIZE_MAX;i++)
		status.stone_numbers[i]=stone_numbers[i];
	status.gamemode=m_gamemode;
	for (i = 0; i < PLAYER_MAX; i++) {
		if (spieler[i] == PLAYER_COMPUTER || spieler[i] == PLAYER_LOCAL)
			status.spieler[i] = spieler[i];
		else {
			int j;
			for (j = 0; j < CLIENTS_MAX; j++)
				if (clients[j] == spieler[i])
					status.spieler[i] = j;
		}
	}
	for (i = 0; i < CLIENTS_MAX; i++) {
		status.client_names[i][0] = '\0';
		if (clients[i] != 0) {
			if (names[i])
				strcpy((char*)status.client_names[i], names[i]);
		}
	}

	send_all((NET_HEADER*)&status,sizeof(status),MSG_SERVER_STATUS);
}

/**
 * Schickt eine Chat-Nachricht an alle Clients, dessen Absender der Server ist
 **/
void CSpielServer::send_server_msg(const char *text)
{
	/* Speicher fuer NET_CHAT reservieren, wo text reinpasst. */
	NET_CHAT *chat=(NET_CHAT*)malloc(sizeof(NET_CHAT)+strlen(text)+1);
	/* Absender ist -1, der Server */
	chat->client=-1;
	/* Text in Nachricht kopieren */
	strcpy((char*)(&chat->text[0]),text);
	chat->length=strlen(text);
	/* Chat-Nachricht an alle Clients. */
	send_all((NET_HEADER*)chat,sizeof(NET_CHAT)+strlen(text)+1,MSG_CHAT);
	free(chat);
}

/**
 * Starte das eigentliche Spiel
 **/
void CSpielServer::start_game()
{
	/* Wenn es bereits laeuft, mache nichts */
	if (m_current_player!=-1)return;
// 	printf("Starting game\n");

	/* Spiel zuruecksetzen */
	if (history)history->delete_all_turns();
	CSpiel::start_new_game(m_gamemode);
	CSpiel::set_stone_numbers(stone_numbers[0],stone_numbers[1],stone_numbers[2],stone_numbers[3],stone_numbers[4]);

	/* Wenn nur mit zwei Farben gespielt wird, nehme Spieler 1 und 3 alle Steine weg */
	if (m_gamemode==GAMEMODE_2_COLORS_2_PLAYERS || m_gamemode==GAMEMODE_DUO)
	{
		for (int n = 0 ; n < STONE_COUNT_ALL_SHAPES; n++){
			get_player(1)->get_stone(n)->set_available(0);
			get_player(3)->get_stone(n)->set_available(0);
		}
	}

	/* Startspieler ist immer Spieler 0 (=Blau) */
	m_current_player=0;
	/* Schicke Server Status, Info ueber Spielstart, sowie aktuellen Spieler los */
	send_server_status();

	NET_START_GAME data;
	send_all((NET_HEADER*)&data,sizeof(data),MSG_START_GAME);
	send_current_player();

	/* Spiel wurde gestartet, timer auf 0 setzen */
	timer.reset();
}

/**
 * Ermittle naechsten Spieler
 **/
void CSpielServer::next_player()
{
	int i;
	for (i=0;i<PLAYER_MAX;i++)
	{
		m_current_player=(m_current_player+1)%PLAYER_MAX;
		/* Wenn der naechste Spieler in der Reihe noch mindestens einen freien Zug hat, 
		   ist dieser dran. Sonst muss er aussetzen, und der uebernaechste wird probiert. */
		if (get_number_of_possible_turns(m_current_player)>0)
		{
// 			printf("Spieler %d ist dran: Hat %d moegliche Zuege.\n",m_current_player,get_number_of_possible_turns(m_current_player));
			return;
		}//else printf("Spieler %d muss aussetzen.\n",m_current_player);
	}
	/* Ist man hier angelangt, hat keiner der PLAYER_MAX Spieler noch freie Zuege.
	   Das Spiel ist vorbei. */
	m_current_player=-1;

	/* Schicke eine MSG_GAME_FINISH Nachricht an die Clients, die ueber das Spielende informiert. */
	NET_GAME_FINISH data;
	send_all((NET_HEADER*)&data,sizeof(data),MSG_GAME_FINISH);

	/* Statusmeldungen auf Konsole ausgeben (z.B. fuer dedicated Server) */
	if (logger){
		logger->logLine("-- Game finished! -- Took %.2f sek. --\n",timer.elapsed());
	}
	for (i=0;i<PLAYER_MAX;i++)
	{
		CPlayer * player=get_player(i);
		if (logger) {
			logger->logLine("Player %d has %d stones left and %d points.\n",i,get_stone_count(i),-player->get_stone_points_left());
		}
	}
}

/**
 * Setzt die Anzahl der Steine des Spiels. Die Zahlen werden lokal gemerkt,
 * damit sie an die Clients geschickt werden koennen.
 **/
void CSpielServer::set_stone_numbers(int einer,int zweier,int dreier,int vierer,int fuenfer)
{
	stone_numbers[0]=einer;
	stone_numbers[1]=zweier;
	stone_numbers[2]=dreier;
	stone_numbers[3]=vierer;
	stone_numbers[4]=fuenfer;
	/* Das CSpiel setzt hier die Player richtig */
	CSpiel::set_stone_numbers(einer,zweier,dreier,vierer,fuenfer);
}


/** 
 * Der Server-Thread, der das Spiel parallel hosten soll 
 * Wird von CSpielServer::run_server(...) aufgerufen
 **/
#ifdef WIN32
DWORD WINAPI LocalServerThread(void* param)
#else
void* LocalServerThread(void* param)
#endif
{
	/* Wir brauchen erst einen Listener */
	CServerListener* listener=(CServerListener*)param;
	/* Der Thread soll sterben, wenn sich ein Spieler verbunden und wieder getrennt hat, 
	   ohne dass das Spiel gestartet wurde */
	bool hadClient=false;
#ifdef WIN32
	srand((unsigned int)time(NULL));
#endif
	do
	{
		sockaddr_storage client;
		listener->wait_for_player(false, &client);
		// Merken, wenn jemals ein Client verunden war
		if (listener->get_game()->num_clients()>=1)hadClient=true;
		// Stirb ab, wenn sich der letzte Client getrennt hat
		if (listener->get_game()->num_clients()==0 && hadClient)
			break;
		// Solange Spieler sammeln, wie das Spiel noch nicht gestartet wurde
	} while(listener->get_game()->current_player()==-1);
	/* Spiel wurde nun begonnen, also keine Verbindungen mehr akzeptieren. */
	listener->close();
	// Nur wenn ueberhaupt ein Client verbunden ist, soll das Spiel laufen
	if (listener->get_game()->num_clients()>=1)listener->get_game()->run();
	// Aufraeumen und raus
	delete listener;
// 	printf("Local server terminating.\n");
	return 0;
}

/**
 * Startet einen Listener, spaltet ihn in einen zweiten Thread ab und kehrt zurueck
 * return 0, bei Erfolg
 * Wird von der GUI aufgerufen, um einen lokalen Server zu starten, fuer single- oder multiplayer
 **/
int CSpielServer::run_server(const char* interface_,int port,int maxhumans,int ki_mode,int width,int height,GAMEMODE gamemode,int einer,int zweier,int dreier,int vierer,int fuenfer,int ki_threads)
{
	// Listener erstellen und einrichten
	CServerListener *listener=new CServerListener();
	int r=listener->init(interface_,port);
	// Bei Fehler hier schon raus
	if (r!=0)
	{
	    delete listener;
	    return errno;
	}

	// Listener fuer neues Spiel vorbereiten
	listener->new_game(maxhumans,ki_mode,gamemode,ki_threads,1);
	listener->get_game()->set_field_size(width, height);
	listener->get_game()->start_new_game(gamemode);
	listener->get_game()->set_stone_numbers(einer,zweier,dreier,vierer,fuenfer);

	// Einen Thread starten, der sich um das Spiel kuemmert
#ifdef WIN32
	DWORD id;
	CloseHandle(CreateThread(NULL,0,LocalServerThread,(void*)listener,0,&id));
#else
	pthread_t thread;
	pthread_create(&thread,NULL,LocalServerThread,(void*)listener);
#endif
	// Erfolg
	return 0;
}




/**
 * Leeren CServerListener erstellen, ohne socket und ohne CSpielServer
 **/
CServerListener::CServerListener()
{
	int i;
	for (i=0;i<LISTEN_SOCKETS_MAX; i++)
		listen_sockets[i]=0;
	num_listen_sockets = 0;
	server=NULL;
}

CServerListener::~CServerListener()
{
	/* Socket schliessen, und CSpielServer loeschen */
	close();
	if (server)delete server;
}

/**
 * Socket schliessen, an dem auf ankommende Verbindungen gelauscht wird
 **/
void CServerListener::close()
{
	int i;
	for (i=0;i<LISTEN_SOCKETS_MAX;i++)
	{
		if (listen_sockets[i])closesocket(listen_sockets[i]);
		listen_sockets[i]=0;
	}
}

/**
 * Erstellt ein socket, bindet es an das Interface interface_ und Port port,
 * und wird fuer ankommende Verbindungen eingerichtet
 * ist interface_==NULL, wird an allen Interfaces gelauscht.
 * Gibt 0 zurueck bei Erfolg, sonst einen Fehlercode (errno)
 **/
int CServerListener::init(const char* interface_,int port)
{
	errno=0;

#ifndef WIN32
	if (port == 0)
	{
		int listen_socket;
		int i;
		struct sockaddr_un my_addr;
		errno=0;
		if (interface_ == NULL) {
			errno = EINVAL;
			return -1;
		}

		num_listen_sockets = 0;
		listen_socket=socket(PF_UNIX,SOCK_STREAM,0);
		if (listen_socket <= 0)
			return errno;

		my_addr.sun_family=AF_UNIX;
		strcpy(my_addr.sun_path, interface_);
		i = 1;
		if (setsockopt(listen_socket,SOL_SOCKET,SO_REUSEADDR,&i,sizeof(i))==-1)
		{}
		if (bind(listen_socket,(struct sockaddr*)&my_addr,sizeof(my_addr))!=0)
		{
			closesocket(listen_socket);
			if (errno) return errno;
		}
		if (listen(listen_socket,5)==0)
		{
			listen_sockets[num_listen_sockets++] = listen_socket;
		} else {
			closesocket(listen_socket);
		}
		if (num_listen_sockets == 0)
		{
			if (errno) return errno;
			return -1;
		}
		/* Erfolg */

		return 0;
	}
#endif


#if (defined HAVE_GETADDRINFO) || (defined WIN32)
	addrinfo hints,*res,*ressave;
	char s_port[32];

	sprintf(s_port,"%d",port);

	memset(&hints,0,sizeof(hints));
	hints.ai_flags=AI_PASSIVE;
	hints.ai_family=AF_UNSPEC;
	hints.ai_socktype=SOCK_STREAM;

	if (getaddrinfo(interface_,s_port,&hints,&res)<0)
		return errno?errno:-1;

	num_listen_sockets = 0;
	ressave=res;
	while (res)
	{
		int listen_socket;
		listen_socket=socket(res->ai_family,res->ai_socktype,res->ai_protocol);
		if (listen_socket>=0)
		{
#ifndef WIN32	
			int i;
			/* Unter Linux doch SO_REUSEADDR verwenden, damit bind auch erfolgt, wenn ein Socket
			  mit dem Port bereits existiert. */
			i = 1;
			if (setsockopt(listen_socket,SOL_SOCKET,SO_REUSEADDR,&i,sizeof(i))==-1)
			{}

			if (res->ai_family == AF_INET6) {
				i = 1;
				if (setsockopt(listen_socket, IPPROTO_IPV6, IPV6_V6ONLY, &i, sizeof(i)) < 0) {
					perror("setsockopt");
				}
			}

#endif
			if (bind(listen_socket,res->ai_addr,res->ai_addrlen)==0)
			{
				/* Mit listen einkommende Verbindungen akzeptieren */
				if (listen(listen_socket,5)==0)
				{
					listen_sockets[num_listen_sockets++] = listen_socket;
				} else {
					closesocket(listen_socket);
				}
			} else {
				closesocket(listen_socket);
			}
		}
		res=res->ai_next;
	}
	if (ressave)
		freeaddrinfo(ressave);
	if (num_listen_sockets == 0)
	{
		if (errno) return errno;
		return -1;
	}

#else

	sockaddr_in addr;

	addr.sin_family=AF_INET;
	addr.sin_port=htons(port);

	/* Entweder an einer IP Addr. lauschen, oder an allen */
	if (interface_)addr.sin_addr.s_addr=inet_addr(interface_);
		else addr.sin_addr.s_addr=0;

	/* Socket erstellen, bei Fehler raus. */
	num_listen_sockets = 0;
	listen_sockets[0]=socket(AF_INET,SOCK_STREAM,0);
	if (listen_sockets[0]==-1)return errno;
	num_listen_sockets = 1;

#ifndef WIN32	
	/* Unter Linux doch SO_REUSEADDR verwenden, damit bind auch erfolgt, wenn ein Socket
	   mit dem Port bereits existiert. */
	int i = 1;
	if (setsockopt(listen_sockets[0],SOL_SOCKET,SO_REUSEADDR,&i,sizeof(i))==-1)return errno;
#endif

	/* Socket an Quelladdresse binden */
	if (bind(listen_sockets[0],(sockaddr*)&addr,sizeof(addr))==-1)return errno;
	if (listen(listen_sockets[0],5)==-1)return errno;
#endif

	/* Erfolg */
	return 0;
}

/**
 * Verarbeitet genau ein Netzwerkereignis, entwede Daten eines Clients, der bereits zum Server
 * verbunden ist, oder eine ankommende Verbindung.
 **/
int CServerListener::wait_for_player(bool verbose, sockaddr_storage *client)
{
	/* Addresse des Clients, der sich gerade verbinden moechte */

	/* Dateidestriptoren der Sockets, einer ist der listen_socket */
	fd_set filedescriptors;
	int retval;
	int max;
	int i,j;

#ifdef WIN32
	int l;
#else
	socklen_t l;
#endif
	l=sizeof(*client);

	/* Fuettere filedescriptors mit den sockets der verbunden Clients, sowie des listen_socket */
	FD_ZERO(&filedescriptors);
	max=listen_sockets[0];
	for (i=0;i<num_listen_sockets;i++)
	{
		FD_SET((unsigned int)listen_sockets[i],&filedescriptors);
		if (max<listen_sockets[i])max=listen_sockets[i];
	}
	for (int i=0;i<CLIENTS_MAX;i++)if (get_game()->clients[i]!=0)
	{
		FD_SET((unsigned int)get_game()->clients[i],&filedescriptors);
		if (max<get_game()->clients[i])max=get_game()->clients[i];
	}

	/* Blockiere unendlich lang, bis Daten an einem Socket vorliegen */
	retval = select(max+1, &filedescriptors, NULL, NULL, NULL);

	if (retval>0)
	{
		/* Gehe Clients durch, ob Daten anliegen. */
		for (i=0;i<CLIENTS_MAX;i++)
			if (get_game()->clients[i]!=0 && FD_ISSET(get_game()->clients[i],&filedescriptors))
		{
			/* Wenn ja, lass den CSpielServer die Daten verarbeiten. */
			get_game()->handle_client(i);
		}

		/* Wenn Daten fuer den listen_socket anliegen, moechte jemand eine Verbindung herstellen */
		for (j=0;j<num_listen_sockets;j++)
			if (FD_ISSET(listen_sockets[j],&filedescriptors))
		{
			/* Akzeptiere die Verbindung, cl ist der neue Client-Socket */
			l=sizeof(*client);
			int cl=accept(listen_sockets[j],(sockaddr*)client,&l);
			if (cl==-1)return -1;

			if (verbose)
			{
#if (defined HAVE_GETADDRINFO) || (defined WIN32)
				char clienthost[NI_MAXHOST];
				clienthost[0]='\0';

				if (logger) {
					logger->logTime();
					logger->logHeader();
					logger->log("Connection from: ");
				}


				/* Erst FQDN aufloesen */
				retval = getnameinfo((sockaddr*)client,l,
					clienthost,sizeof(clienthost),
					NULL,0,
					NI_NAMEREQD);

				if (retval == 0 && logger)
					logger->log("%s, ",clienthost);

				/* Dann IP aufloesen */
				getnameinfo((sockaddr*)client,l,
					clienthost,sizeof(clienthost),
					NULL,0,
					NI_NUMERICHOST);

				if (logger)
					logger->log("%s\n",clienthost);
#else
				hostent *host;
				host=gethostbyaddr((char*)client->sin_addr.s_addr,sizeof(client->sin_addr.s_addr),AF_INET);
				unsigned int a,b,c,d,i;
				i=ntohl(client->sin_addr.s_addr);
				a=(i>>24)&0xFF;
				b=(i>>16)&0xFF;
				c=(i>>8)&0xFF;
				d=(i>>0)&0xFF;
				printf("Connection from: ");
				if (host)printf("%s, ",host->h_name);
				printf("%d.%d.%d.%d\n",a,b,c,d);
				if (CLogger::logfile)
				{
					fprintf(CLogger::logfile,"Connection from: ");
					if (host)fprintf(CLogger::logfile,"%s, ",host->h_name);
					fprintf(CLogger::logfile,"%d.%d.%d.%d\n",a,b,c,d);
					CLogger::flush();
				}
#endif
			}

			/* Fuege den neuen Socket dem CSpielServer als Client hinzu */
			get_game()->add_client(cl);
		}
	}

	/* Erfolg */
	return 0;
}

/**
 * Erstellt ein neues Spiel mit max_humans menschlichen Spielern, der Rest ist fuer Computer
 * reserviert
 **/
void CServerListener::new_game(int max_humans,int ki_mode,GAMEMODE gamemode,int ki_threads,int forceDelay)
{
//    if (server)delete server;
	server=new CSpielServer(max_humans,ki_mode,gamemode,forceDelay);
	server->set_ki_threads(ki_threads);
}

