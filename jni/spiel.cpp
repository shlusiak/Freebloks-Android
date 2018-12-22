#ifdef HAVE_CONFIG_H
  #include "config.h"
#endif

#include "spiel.h"

#include "constants.h"


const int DEFAULT_FIELD_SIZE_X = 20;
const int DEFAULT_FIELD_SIZE_Y = 20;


CSpiel::CSpiel(){
	CSpiel::m_game_field = NULL;
	CSpiel::m_field_size_y = DEFAULT_FIELD_SIZE_Y;
	CSpiel::m_field_size_x = DEFAULT_FIELD_SIZE_X;
}

CSpiel::CSpiel(int width, int height) {
	m_field_size_x = width;
	m_field_size_y = height;
	CSpiel::m_game_field = new TSingleField[CSpiel::m_field_size_y * CSpiel::m_field_size_x];
}


CSpiel::CSpiel(const int player_team1_1, const int player_team1_2, const int player_team2_1, const int player_team2_2){
	CSpiel::m_game_field = NULL;
	CSpiel::m_field_size_y = DEFAULT_FIELD_SIZE_Y;
	CSpiel::m_field_size_x = DEFAULT_FIELD_SIZE_X;
	start_new_game(GAMEMODE_4_COLORS_4_PLAYERS);
	CSpiel::set_teams(player_team1_1, player_team1_2, player_team2_1, player_team2_2);
}

void CSpiel::follow_situation(int vorher_playernumber, const CSpiel* vorher_situation, const CTurn* turn) {
	memcpy(m_game_field, vorher_situation->get_field_pointer(), CSpiel::m_field_size_x * CSpiel::m_field_size_y);
	memcpy(m_player,vorher_situation->m_player,sizeof(m_player));
	set_stone(turn);
}

const int CSpiel::get_player_start_x(const int playernumber)const{
	switch (playernumber) {
	case 0 :
	case 1 : return 0;
	default: return CSpiel::m_field_size_x-1;
	}
}

const int CSpiel::get_player_start_y(const int playernumber)const{
	switch (playernumber){
	case 1 :
	case 2 : return 0;
	default: return CSpiel::m_field_size_y -1;
	}
}

void CSpiel::set_stone_numbers(int8 stone_numbers[]){
	for (int n = 0 ; n < STONE_COUNT_ALL_SHAPES; n++){  
		for (int p = 0; p < PLAYER_MAX; p++){
			CStone* stone = CSpiel::m_player[p].get_stone(n);
			stone->set_available(stone_numbers[n]);
		}
	}

	CSpiel::refresh_player_data();
}

void CSpiel::set_teams(int player_team1_1, int player_team1_2, int player_team2_1, int player_team2_2){
	
	#ifdef _DEBUG 
		//�berpr�fung!
		for (int p = 0; p < PLAYER_MAX; p++){
			int count = 0;
			if (player_team1_1 == p) count++;
			if (player_team1_2 == p) count++;
			if (player_team2_1 == p) count++;
			if (player_team2_2 == p) count++;
			if (count != 1) error_exit("Ung�ltige team�bergabe!", 20);
		}
	#endif

	CSpiel::m_player[player_team1_1].set_teammate(player_team1_2);
	CSpiel::m_player[player_team1_2].set_teammate(player_team1_1);
	CSpiel::m_player[player_team1_1].set_nemesis(player_team2_1);
	CSpiel::m_player[player_team1_2].set_nemesis(player_team2_1);

	CSpiel::m_player[player_team2_1].set_teammate(player_team2_2);
	CSpiel::m_player[player_team2_2].set_teammate(player_team2_1);
	CSpiel::m_player[player_team2_1].set_nemesis(player_team1_1);
	CSpiel::m_player[player_team2_2].set_nemesis(player_team1_1);
}





CSpiel::~CSpiel(){
	delete [] CSpiel::m_game_field;
}




void CSpiel::start_new_game(GAMEMODE gamemode){
	init_field();
	set_seeds(gamemode);
	for (int n = 0; n < PLAYER_MAX; n++){
		CSpiel::m_player[n].init(this, n);
	}
}



void CSpiel::refresh_player_data(){
	for (int n = 0; n < PLAYER_MAX; n++){
		CSpiel::m_player[n].refresh_data(this);
	}
}


void CSpiel::init_field(){
	if (m_game_field != NULL) delete[] CSpiel::m_game_field;
	CSpiel::m_game_field = new TSingleField[CSpiel::m_field_size_y * CSpiel::m_field_size_x];
	memset(m_game_field, 0, sizeof(TSingleField) * m_field_size_y * m_field_size_x);
}

void CSpiel::set_seeds(GAMEMODE gamemode) {
	#define set_seed(x, y, player) \
		if (get_game_field(player, y, x) == FIELD_FREE) \
			set_game_field(y, x, PLAYER_BIT_ALLOWED[player]);
	if (gamemode == GAMEMODE_DUO || gamemode == GAMEMODE_JUNIOR) {
		set_seed(4, m_field_size_y - 5, 0);
		set_seed(m_field_size_x - 5, 4, 2);
	} else {
		for (int p = 0; p < PLAYER_MAX; p++){
			set_seed(get_player_start_x(p), get_player_start_y(p), p);
		}
	}
	#undef set_seed
}

/** r�ckgabe �ndern in bool?! **/
TSingleField CSpiel::is_valid_turn(CStone* stone, int playernumber, int startY, int startX)const{
	#ifdef _DEBUG
		if (playernumber < 0 || playernumber >= PLAYER_MAX) error_exit("Falsche Spielerzahl", playernumber); //debug
	#endif
	TSingleField valid = FIELD_DENIED;
	TSingleField field_value;

	for (int y = 0; y < stone->get_stone_size(); y++){
		for (int x = 0; x < stone->get_stone_size(); x++){
			if (stone->get_stone_field(y,x) != STONE_FIELD_FREE) {
				if (!is_position_inside_field(y + startY, x + startX)) return FIELD_DENIED;

				/*TODO::: eventuell ein array �bergeben*/
				field_value = CSpiel::get_game_field (playernumber, y + startY , x + startX);
				if (field_value == FIELD_DENIED) return FIELD_DENIED;
				if (field_value == FIELD_ALLOWED) valid = FIELD_ALLOWED;
			}
		}
	}
	return valid;
}

TSingleField CSpiel::is_valid_turn(const CTurn* turn){
	int playernumber = turn->get_playernumber();
	CStone* stone = CSpiel::m_player[playernumber].get_stone(turn->get_stone_number());
	stone->mirror_rotate_to(turn->get_mirror_count(), turn->get_rotate_count());
	return is_valid_turn(stone, playernumber, turn->get_y(), turn->get_x());
}

void CSpiel::free_gamefield(int y, int x){
	CSpiel::set_game_field(y, x, 0);
}


void CSpiel::set_single_stone_for_player(const int playernumber, const int startY, const int startX){
	#ifdef _DEBUG
		if (playernumber < 0 || playernumber >= PLAYER_MAX) error_exit("Falsche Spielerzahl", playernumber); //debug
	#endif

	CSpiel::set_game_field(startY , startX, PLAYER_BIT_HAVE_MIN | playernumber);
	for (int y = startY-1; y <= startY+1; y++)if (y>=0 && y<m_field_size_y) {
		for (int x = startX-1; x <= startX+1; x++)if (x>=0 && x<m_field_size_x){
			if (get_game_field(playernumber, y, x) != FIELD_DENIED){
				if (y != startY && x != startX){
					CSpiel::m_game_field[y * CSpiel::m_field_size_x + x] |= PLAYER_BIT_ALLOWED[playernumber];
				}else{
					CSpiel::m_game_field[y * CSpiel::m_field_size_x + x] &= ~PLAYER_BIT_ADDR[playernumber];
					CSpiel::m_game_field[y * CSpiel::m_field_size_x + x] |= PLAYER_BIT_DENIED[playernumber];
				}
			}
		}
	}
}

/** r�ckgabe zu bool?! **/
TSingleField CSpiel::set_stone(const CTurn* turn){
	#ifdef _DEBUG
		if (turn == 0) error_exit("turn ist nullpointer!", 20); //debug
	#endif

	int playernumber = turn->get_playernumber();
	CStone* stone = CSpiel::m_player[playernumber].get_stone(turn->get_stone_number());
	stone->mirror_rotate_to(turn->get_mirror_count(), turn->get_rotate_count());
	return set_stone(stone, playernumber, turn->get_y(), turn->get_x());
}



/** r�ckgabe zu bool?! **/
TSingleField CSpiel::set_stone(CStone* stone, int playernumber, int startY, int startX){
#ifdef _DEBUG
	if (playernumber < 0 || playernumber >= PLAYER_MAX) error_exit("Falsche Spielerzahl", playernumber); //debug
#endif
//	if (is_valid_turn(stone, playernumber, startY, startX) == FIELD_DENIED) return FIELD_DENIED;

	for (int y = 0; y < stone->get_stone_size(); y++){
		for (int x = 0; x < stone->get_stone_size(); x++){
			if (stone->get_stone_field(y,x) != STONE_FIELD_FREE) {
				CSpiel::set_single_stone_for_player(playernumber, startY+y, startX+x);
			}
		}
	}
	stone->available_decrement();
	refresh_player_data();
	return FIELD_ALLOWED;
}




void CSpiel::undo_turn(CTurnpool* turnpool, GAMEMODE gamemode){
	CTurn* turn = turnpool->get_last_turn();
	CStone* stone = CSpiel::m_player[turn->get_playernumber()].get_stone(turn->get_stone_number());
	int x, y;
	stone->mirror_rotate_to(turn->get_mirror_count(), turn->get_rotate_count());

	#ifdef _DEBUG
		//check valid
		if (turn == NULL) error_exit("Kein turn", 42);
		for (x = 0; x < stone->get_stone_size(); x++){
			for (y = 0; y < stone->get_stone_size(); y++){
				if (stone->get_stone_field(y, x) != STONE_FIELD_FREE){
					if (CSpiel::get_game_field(turn->get_y() + y, turn->get_x() + x) != turn->get_playernumber()) {
						printf("y: %d, x: %d\n", turn->get_y() + y, turn->get_x() +x);
						error_exit("�bergebener Turnpool fehlerhaft (undo turn)", 44);//return false;
					}
				}
			}
		}
	#endif

	//delete stone
	for (x = 0; x < stone->get_stone_size(); x++){
		for (y = 0; y < stone->get_stone_size(); y++){
			if (stone->get_stone_field(y, x) != STONE_FIELD_FREE){
				CSpiel::free_gamefield(turn->get_y() + y, turn->get_x() + x);
			}
		}
	}

	//redraw gamefield
	for (x = 0; x < CSpiel::get_field_size_x(); x++){
		for (y = 0; y < CSpiel::get_field_size_y(); y++){
			if (CSpiel::get_game_field(y, x) == FIELD_FREE ){
				CSpiel::free_gamefield(y, x);
			}
		}
	}
	for (x = 0; x < CSpiel::get_field_size_x(); x++){
		for (y = 0; y < CSpiel::get_field_size_y(); y++){
			if (CSpiel::get_game_field(y, x) != FIELD_FREE ){
				CSpiel::set_single_stone_for_player(CSpiel::get_game_field(y, x), y, x);
			}
		}
	}

	set_seeds(gamemode);
	stone->available_increment();
	refresh_player_data();
	//end redraw
	turnpool->delete_last();
}
