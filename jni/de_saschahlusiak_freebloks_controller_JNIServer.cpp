#ifdef HAVE_CONFIG_H
#include "config.h"
#endif

#include "de_saschahlusiak_freebloks_controller_JNIServer.h"
#include <pthread.h>
#include "spielserver.h"


/**
 * Funktion kriegt einen CServerListener und wartet so lange, bis das Spiel gestartet wurde
 **/
static int EstablishGame(CServerListener* listener)
{
	int ret;
	do
	{
		sockaddr_storage client;
		client.ss_family = AF_UNSPEC;
		/* Listener auf einen Client warten oder eine Netzwerknachricht verarbeiten lassen */
		ret=listener->wait_for_player(true, &client);
		/* Bei Fehler: Raus hier */
		if (ret==-1)
		{
			perror("wait(): ");
			return -1;
		}
		/* Solange, wie kein aktueller Spieler festgelegt ist: Spiel laeuft noch nicht */
	}while (listener->get_game()->current_player()==-1);
	return 0;
}

void* gameRunThread(void* param)
{
	CServerListener *listener = (CServerListener*)param;
	D("thread 1");

	if (EstablishGame(listener) == -1)
		return NULL;

	D("thread 2");

	CSpielServer* game = listener->get_game();

	listener->close();
	
	game->run();

	delete listener;

	return NULL;
}


static int max_humans = 4;
static GAMEMODE gamemode = GAMEMODE_4_COLORS_4_PLAYERS;
static int ki_threads = 3;
static int force_delay = 1;
static int port = 59995;
static char* _interface = NULL;

JNIEXPORT jint JNICALL Java_de_saschahlusiak_freebloks_controller_JNIServer_native_1run_1server
  (JNIEnv * je, jclass jc, jint ki_mode)
{
	int ret;
	pthread_t pt;

	D("init 1");
	CServerListener* listener=new CServerListener();
	D("init 2");
	ret=listener->init(_interface, port);
	if (ret!=0)
	{
	    return -1;
	}

	D("init 3");

	listener->new_game(max_humans, ki_mode, gamemode, ki_threads, force_delay);

	D("starting thread");

	if (pthread_create(&pt,NULL,gameRunThread,(void*)listener))
		perror("pthread_create");
	if (pthread_detach(pt))perror("pthread_detach");
	return 0;
}
