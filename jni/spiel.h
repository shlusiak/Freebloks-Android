#ifndef ____SPIEL___H__
#define ____SPIEL___H__


#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include "stone.h"
#include "player.h"
#include "turn.h"
#include "constants.h"
#include "ki.h"
#include "turnpool.h"
#include "network.h"

class CSpiel{

	private:
		int m_field_size_y;
		int m_field_size_x;

		CPlayer m_player[PLAYER_MAX];
		TSingleField* m_game_field;
				
		const bool is_position_inside_field(const int y, const int x)const;
		void refresh_player_data();

		void set_single_stone_for_player(const int playernumber, const int y, const int x);
		void free_gamefield(int y, int x);

	public:

		CSpiel();
		CSpiel(const int width, const int height);
		CSpiel(const int player_team1_1, const int player_team1_2, const int player_team2_1, const int player_team2_2);

		virtual ~CSpiel();

		void follow_situation(int vorher_playernumber, const CSpiel* vorher_situation, const CTurn* turn);
		void init_field();
		void set_seeds(enum GAMEMODE gamemode);
		void set_game_field(const int y, const int x, const TSingleField value);

		/*PLAYER*/
		const int get_player_start_x(const int playernumber)const;
		const int get_player_start_y(const int playernumber)const;


		const int get_number_of_possible_turns(const int playernumber)const;
		const int get_stone_points_left(const int playernumber)const ;
		const int get_position_points(const int playernumber)const ;
		const int get_stone_count(const int playernumber)const ;
		const int get_teammate(const int playernumber)const ;
		const int get_nemesis(const int playernumber)const ;


		void start_new_game(GAMEMODE gamemode);
		void set_field_size(int x, int y) { m_field_size_x = x; m_field_size_y = y; }

		const int get_field_size_x()const;
		const int get_field_size_y()const;
		const int get_player_max()const;
		const int get_stone_count_max()const;
		const int get_max_stone_size()const;

		void set_teams(int player_team1_1, int player_team1_2, int player_team2_1, int player_team2_2);
		virtual void set_stone_numbers(int8 stone_numbers[]);
		virtual void set_stone_numbers(int einer, int zweier, int dreier, int vierer, int fuenfer);
		
		CPlayer* get_player(const int playernumber);
		TSingleField is_valid_turn(CStone* stone, int player, int y, int x)const;
		TSingleField is_valid_turn(const CTurn* turn);

		const TSingleField get_game_field(const int playernumber, const int y, const int x)const; //f�r spielerr�ckgaben
		const TSingleField get_game_field(const int y, const int x)const; //f�r feldr�ckgaben
		
		const char get_game_field_value(const int y, const int x)const; //f�r �bergabe an andere spiel-klassen
		TSingleField* get_field_pointer()const;
		
		TSingleField set_stone(CStone* stone, int playernumber, int y, int x);
		TSingleField set_stone(const CTurn* turn);

		void undo_turn(CTurnpool* turnpool, GAMEMODE gamemode);
};


inline
//f�r folgesituationen von CTurn
const char CSpiel::get_game_field_value(const int y, const int x)const{
	return CSpiel::m_game_field[y * CSpiel::m_field_size_x + x];
}

inline
TSingleField* CSpiel::get_field_pointer()const{
	return CSpiel::m_game_field;
}

inline
const TSingleField CSpiel::get_game_field(const int playernumber, const int y, const int x)const{
	#ifdef _DEBUG
		if (playernumber < 0 || playernumber >= PLAYER_MAX) error_exit("Falsche Spielerzahl", playernumber); //debug
	#endif

	TSingleField wert = get_game_field_value(y,x);
	if (wert >= PLAYER_BIT_HAVE_MIN) return FIELD_DENIED;
	wert &= PLAYER_BIT_ADDR[playernumber];
	if (wert == 0) return FIELD_FREE;
	if (wert > PLAYER_BIT_ALLOWED[playernumber]) return FIELD_DENIED;
	return FIELD_ALLOWED;
}


inline
const TSingleField CSpiel::get_game_field(const int y, const int x)const{
	const TSingleField wert = CSpiel::get_game_field_value(y,x);
	if (wert < PLAYER_BIT_HAVE_MIN) return FIELD_FREE;
	return wert & 3;
}

inline
const int CSpiel::get_field_size_x()const {
	return CSpiel::m_field_size_x;
}

inline
const int CSpiel::get_field_size_y()const{
	return CSpiel::m_field_size_y;
}

inline
const int CSpiel::get_player_max()const{
	return PLAYER_MAX;
}

inline
const int CSpiel::get_stone_count(const int playernumber)const {
	return CSpiel::m_player[playernumber].get_stone_count();
}

inline
const int CSpiel::get_nemesis(const int playernumber)const {
	return CSpiel::m_player[playernumber].get_nemesis();
}

inline
const int CSpiel::get_teammate(const int playernumber)const {
	return CSpiel::m_player[playernumber].get_teammate();
}

inline
void CSpiel::set_game_field(const int y, const int x, const TSingleField value){
	m_game_field[y * CSpiel::m_field_size_x + x] = value;
}

inline
CPlayer* CSpiel::get_player(const int playernumber){
	#ifdef _DEBUG
		if (playernumber < 0 || playernumber >= PLAYER_MAX) error_exit("Falsche Spielerzahl", playernumber); //debug
	#endif
	return &m_player[playernumber];
}

inline
const bool CSpiel::is_position_inside_field(const int y, const int x)const {
	return (y >= 0 && y < CSpiel::m_field_size_y && x >= 0 && x < CSpiel::m_field_size_x);
}

inline
const int CSpiel::get_max_stone_size()const {
	return STONE_SIZE_MAX;
}


inline
const int CSpiel::get_number_of_possible_turns(const int playernumber)const{
	#ifdef _DEBUG
		if (playernumber < 0 || playernumber >= PLAYER_MAX) error_exit("Falsche Spielerzahl", playernumber); //debug
	#endif
	return CSpiel::m_player[playernumber].get_number_of_possible_turns();
}

inline
const int CSpiel::get_stone_points_left(const int playernumber)const{
	#ifdef _DEBUG
		if (playernumber < 0 || playernumber >= PLAYER_MAX) error_exit("Falsche Spielerzahl", playernumber); //debug
	#endif
	return CSpiel::m_player[playernumber].get_stone_points_left();
}

inline
const int CSpiel::get_position_points(const int playernumber)const{
	#ifdef _DEBUG
		if (playernumber < 0 || playernumber >= PLAYER_MAX) error_exit("Falsche Spielerzahl", playernumber); //debug
	#endif
	return CSpiel::m_player[playernumber].get_position_points();
}







#endif
