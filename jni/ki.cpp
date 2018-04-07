#ifdef HAVE_CONFIG_H
  #include "config.h"
#endif

#ifdef WIN32
#include <windows.h>
#endif
#ifdef HAVE_LIBPTHREAD
#include <pthread.h>
#endif
#include "ki.h"

#include "spiel.h"
#include "timer.h"

#define BIGGEST_X_STONES 9



// fast identisch mit der CPlayer::calculate_possible_turns
void CKi::calculate_possible_turns(const CSpiel* spiel, CStone* stone, const char playernumber){
	for (int x = 0; x < spiel->get_field_size_x(); x++){
		for (int y = 0; y < spiel->get_field_size_y(); y++){

#ifdef _DEBUG
			unsigned char wert = spiel->get_game_field_value(y, x);
			if (wert < PLAYER_BIT_HAVE_MIN){
				for (int p = 0; p < 4; p++){
					if ((wert & PLAYER_BIT_ADDR[p]) == PLAYER_BIT_ADDR[p])
						error_exit("Hier stinkt etwas nicht!", 01);
				}
			}
#endif

			if (spiel->get_game_field(playernumber, y, x) == FIELD_ALLOWED){
				CKi::calculate_possible_turns_in_position(spiel, stone, playernumber, y, x);
			}
		}
	}
}


void CKi::calculate_possible_turns_in_position(const CSpiel* spiel, CStone* stone, const char playernumber, const int fieldY, const int fieldX){
	int mirror;

	int rotate_count = stone->get_rotate_counter();
	int mirror_count = stone->get_mirror_counter();
	//int shape = stone->get_stone_shape();

	if (stone->get_mirrorable() == MIRRORABLE_IMPORTANT) mirror = 1;
	else mirror = 0;

	for (int m = 0; m <= mirror; m++){
		for (int r = 0; r < stone->get_rotateable(); r++){
			stone->mirror_rotate_to(m, r);
			for (int x = 0; x < stone->get_stone_size(); x++){
				for (int y = 0; y < stone->get_stone_size(); y++){

					if (stone->get_stone_field(y, x) == STONE_FIELD_ALLOWED) {  //es wird get_stone_field benutzt, da gedreht wurde
						if (spiel->is_valid_turn(stone, playernumber, fieldY-y, fieldX-x) == FIELD_ALLOWED){
							CKi::m_turnpool.add_turn(playernumber, stone, fieldY-y, fieldX-x);
						}
					}
				}
			}
		}
	}
	stone->mirror_rotate_to(mirror_count, rotate_count);
}

struct THREADDATA
{
	CKi *ki;
	int from,to;
	int best_points;
	char current_player;
	int ki_fehler;
	CTurn* best;
	CSpiel *spiel;
};

#ifdef WIN32
DWORD WINAPI kiThread(LPVOID p)
#else
void* kiThread(void* p)
#endif
{
	THREADDATA *data=(THREADDATA*)p;
	CSpiel spiel(data->spiel->get_field_size_x(), data->spiel->get_field_size_y());

	int new_points;
#ifdef HAVE_PTHREAD_CREATE
	if (data->from>data->to)pthread_exit((void*)0);
#else
	if (data->from>data->to)return NULL;
#endif

	spiel.follow_situation(data->current_player, data->spiel, data->ki->m_turnpool.get_turn(data->from));
	data->best_points = CKi::get_ultimate_points(&spiel, data->current_player, data->ki_fehler, data->ki->m_turnpool.get_turn(data->from)); //Bewertung hier!!!
	data->best = data->ki->m_turnpool.get_turn(data->from);

	for (int n = data->from+1; n <= data->to; n++){
		spiel.follow_situation(data->current_player, data->spiel, data->ki->m_turnpool.get_turn(n));
		new_points = CKi::get_ultimate_points(&spiel, data->current_player, data->ki_fehler, data->ki->m_turnpool.get_turn(n)); //Bewertung hier!!!

		if (new_points >= data->best_points) {
			data->best = data->ki->m_turnpool.get_turn(n);
			data->best_points = new_points;
		}
	}
#ifdef HAVE_PTHREAD_CREATE
	pthread_exit((void*)0);
#endif
	return NULL;
}

CTurn* CKi::get_ultimate_turn(CSpiel* spiel, const char current_player, const int ki_fehler){
	CKi::build_up_turnpool_biggest_x_stones(spiel, current_player, BIGGEST_X_STONES);

	CTurn* best;
	int best_points;
	int i;
#ifdef HAVE_PTHREAD_CREATE
	pthread_t threads[8];
#elif defined WIN32
	HANDLE threads[8];
#endif
	volatile THREADDATA data[8];
	if (num_threads>8) num_threads = 8;

// 	printf("AI using %d threads\n",num_threads);
	for (i=0;i<num_threads;i++)
	{
		data[i].ki=this;
		data[i].best=NULL;
		data[i].best_points=0;
		data[i].current_player=current_player;
		data[i].ki_fehler=ki_fehler;
		data[i].spiel=spiel;

		data[i].from=2+i*(CKi::m_turnpool.get_number_of_stored_turns()-1)/num_threads;
		data[i].to=2+(i+1)*(CKi::m_turnpool.get_number_of_stored_turns()-1)/num_threads-1;
		if (i==num_threads-1)data[i].to=CKi::m_turnpool.get_number_of_stored_turns();

#ifdef WIN32
		DWORD tid;
		threads[i]=CreateThread(NULL,0,kiThread,(void*)&data[i],0,&tid);
#elif defined HAVE_PTHREAD_CREATE
		pthread_create(&threads[i],NULL,kiThread,(void*)&data[i]);
#else
		kiThread((void*)&data[i]);
#endif
	}

	CSpiel follow_situation(data->spiel->get_field_size_x(), data->spiel->get_field_size_y());
	best = CKi::m_turnpool.get_turn(1);
	follow_situation.follow_situation(current_player, spiel, best);

	best_points = get_ultimate_points(&follow_situation, current_player, ki_fehler, best); //Bewertung hier!!!

	for (i=0;i<num_threads;i++)
	{
#ifdef HAVE_PTHREAD_CREATE
		int *pi;
		pthread_join(threads[i],(void**)&pi);
#elif defined WIN32
		WaitForSingleObject(threads[i],INFINITE);
#endif
		if (data[i].best_points>best_points && data[i].best!=NULL)
		{
			best_points=data[i].best_points;
			best=data[i].best;
		}
	}

	return best;
}


void CKi::build_up_turnpool_biggest_x_stones(CSpiel* spiel, const char playernumber, const int max_stored_stones){
	m_turnpool.begin_add();
	int stored_stones = 0;
	int stored_turns = 0;
	for (int n = STONE_COUNT_ALL_SHAPES -1; n >= 0; n--){
		CStone* stone = spiel->get_player(playernumber)->get_stone(n);
		if (stone->get_available()){
			calculate_possible_turns(spiel, stone, playernumber);
			if (m_turnpool.get_number_of_stored_turns() > stored_turns){
				stored_stones++;
				stored_turns = m_turnpool.get_number_of_stored_turns();
				if (stored_stones >= max_stored_stones) {
					m_turnpool.end_add();
					return;
				}
			}
		}
	}
	m_turnpool.end_add();
}



int CKi::get_distance_points(CSpiel* follow_situation, const char playernumber, const CTurn* turn){
	CStone* stone = follow_situation->get_player(playernumber)->get_stone(turn->get_stone_number());
	int summe = abs(follow_situation->get_player_start_x(playernumber) - turn->get_x() + stone->get_stone_size()/2);
	summe += abs(follow_situation->get_player_start_y(playernumber) - turn->get_y()+ stone->get_stone_size()/2);
	return summe;
}


int CKi::get_ultimate_points(CSpiel* follow_situation, const char playernumber, const int ki_fehler, const CTurn* turn){
	int summe = 0;
	for (int p = 0; p < PLAYER_MAX; p++){
		if (p != playernumber){
			if (p != follow_situation->get_teammate(playernumber)){
				summe -= follow_situation->get_position_points(p);
			}
		}else{
			summe += follow_situation->get_position_points(p);
			summe -= follow_situation->get_stone_points_left(p)*175;
		}
	}
	summe += get_distance_points(follow_situation, playernumber, turn) * 20;
	return ((100+(rand() % ((ki_fehler)+1))) * summe) /100;
}



CTurn* CKi::get_ki_turn(CSpiel* spiel, char playernumber, int ki_fehler){
	if (spiel->get_number_of_possible_turns(playernumber) == 0) return NULL;
	return CKi::get_ultimate_turn(spiel, playernumber, ki_fehler);
}
