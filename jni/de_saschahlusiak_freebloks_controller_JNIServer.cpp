#ifdef HAVE_CONFIG_H
#include "config.h"
#endif

#include <unistd.h>
#include "de_saschahlusiak_freebloks_controller_JNIServer.h"
#include <pthread.h>
#include "spielserver.h"


JNIEXPORT jint JNICALL Java_de_saschahlusiak_freebloks_controller_JNIServer_get_1number_1of_1processors
  (JNIEnv *je, jclass jc)
{
	return sysconf(_SC_NPROCESSORS_CONF);
}

/**
 * Funktion kriegt einen CServerListener und wartet so lange, bis das Spiel gestartet wurde
 **/
static int EstablishGame(CServerListener* listener)
{
	int ret;
	do
	{
		/* Listener auf einen Client warten oder eine Netzwerknachricht verarbeiten lassen */
		ret=listener->wait_for_player(false, NULL);
		/* Bei Fehler: Raus hier */
		if (ret==-1)
		{
			perror("wait(): ");
			return -1;
		}
		if (listener->get_game()->num_clients() <= 0)
			return -1;
		/* Solange, wie kein aktueller Spieler festgelegt ist: Spiel laeuft noch nicht */
	}while (listener->get_game()->current_player()==-1);
	return 0;
}

void* gameRunThread(void* param)
{
	CServerListener *listener = (CServerListener*)param;

	if (EstablishGame(listener) == -1) {
		listener->close();
		delete listener;
		D("game not established");
		return NULL;
	}


	CSpielServer* game = listener->get_game();

	listener->close();

	game->run();

	delete listener;
	D("server thread going down");

	return NULL;
}


static int max_humans = 4;
static int force_delay = 1;
static int port = 59995;
static char* _interface = NULL;

static int8 JUNIOR_STONE_SET[STONE_COUNT_ALL_SHAPES] = {
					2,
					2,
					2, 2,
					2, 2, 2, 2, 2,
					2, 0, 0, 0, 0, 0, 0, 0, 2, 2, 0, 0
			};


JNIEXPORT jint JNICALL Java_de_saschahlusiak_freebloks_controller_JNIServer_native_1resume_1server
  (JNIEnv *je, jclass jc, jint field_size_x, jint field_size_y, jint current_player, jintArray spieler, jintArray field_data, jintArray player_data, jint gamemode, jint ki_mode, jint ki_threads)
{
	int ret;
	pthread_t pt;

	int i,j;

	CServerListener* listener=new CServerListener();
	CSpielServer* game;

	ret=listener->init(_interface, port);
	if (ret!=0)
	{
	    return -1;
	}
	listener->new_game(max_humans, ki_mode, (GAMEMODE)(int)gamemode, ki_threads, force_delay);
	game = listener->get_game();
	game->set_field_size(field_size_x, field_size_y);

	game->start_new_game((GAMEMODE)(int)gamemode);

	/* copy spieler to game, map local players to client 0 */
	jint *tmp = je->GetIntArrayElements(spieler, 0);
	for (i = 0; i < PLAYER_MAX; i++) {
		if (tmp[i] == PLAYER_LOCAL)
			game->setSpieler(i, 0); /* local players are mapped to first client */
		else
			game->setSpieler(i, PLAYER_COMPUTER);
	}
	/* set current player */
	game->setCurrentPlayer(current_player);
	/* set field data */
	tmp = je->GetIntArrayElements(field_data, 0);
	for (i = 0; i < field_size_x; i++)
		for (j = 0; j < field_size_y; j++) {
			game->set_game_field(j, i, tmp[j * field_size_x + i]);
		}

	tmp = je->GetIntArrayElements(player_data, 0);
	for (i = 0; i < PLAYER_MAX; i++)
		for (j = 0; j < STONE_COUNT_ALL_SHAPES; j++) {
			game->get_player(i)->get_stone(j)->set_available(tmp[i * STONE_COUNT_ALL_SHAPES + j]);
		}

	if (pthread_create(&pt,NULL,gameRunThread,(void*)listener))
		perror("pthread_create");
	if (pthread_detach(pt))perror("pthread_detach");
	return 0;
}



JNIEXPORT jint JNICALL Java_de_saschahlusiak_freebloks_controller_JNIServer_native_1run_1server
  (JNIEnv * je, jclass jc, jint gamemode, jint field_size_x, jint field_size_y, jint ki_mode, jint ki_threads)
{
	int ret;
	pthread_t pt;

	CServerListener* listener=new CServerListener();
	ret=listener->init(_interface, port);
	if (ret!=0)
	{
	    return -1;
	}

	listener->new_game(max_humans, ki_mode, (GAMEMODE)gamemode, ki_threads, force_delay);
	listener->get_game()->set_field_size(field_size_x, field_size_y);
	if (gamemode == GAMEMODE_JUNIOR)
	{
		listener->get_game()->set_stone_numbers(JUNIOR_STONE_SET);
	}
	listener->get_game()->start_new_game((GAMEMODE)gamemode);

	if (pthread_create(&pt,NULL,gameRunThread,(void*)listener))
		perror("pthread_create");
	if (pthread_detach(pt))perror("pthread_detach");
	return 0;
}
