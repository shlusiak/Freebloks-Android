#ifndef ___PLAYER____H_
#define ___PLAYER____H_

#include <stdlib.h>
#include "stone.h"
#include "turn.h"
#include "turnpool.h"



class CSpiel;

class CPlayer{

	private:
		
		int m_stone_points_left;
		int m_stone_count;
		int m_number_of_possible_turns;
		int m_position_points;

		int m_teammate;
		int m_nemesis;
		int m_number;
	
		CStone m_stone[STONE_COUNT_ALL_SHAPES]; // eventuell auf bool verkleinerbar?!
	public:
		
		CPlayer();

		void init(const CSpiel* spiel, const int playernumber); //unbedingt ausf�hren!
		void refresh_data(const CSpiel* spiel);	//wird von init automatsich aufgerufen!
												//berechnet jetzt auch die m�glichen z�ge!

		const int get_number()const; //liefert jetzt einen wert von 0 bis 3!
		const int get_stone_points_left()const;
		const int get_position_points()const; //gibt eine situationsbewertung zur�ck
		const int get_number_of_possible_turns()const;
		const int get_stone_count()const;
		
		const int get_teammate()const;
		const int get_nemesis()const;
		void set_teammate(int playernumber);
		void set_nemesis(int playernumber);

		CStone* get_stone(int stonenumber); //stonenumber von 0 bis (STONE_COUNT_MAX-1)!
};


inline
CPlayer::CPlayer() 
:m_nemesis(-1), m_teammate(-1)
{
}


inline 
CStone* CPlayer::get_stone(const int stonenumber){ 
	return &CPlayer::m_stone[stonenumber];
}


inline
const int CPlayer::get_number_of_possible_turns()const{
	return CPlayer::m_number_of_possible_turns;
}


inline
const int CPlayer::get_stone_points_left()const{
	return CPlayer::m_stone_points_left;
}


inline
const int CPlayer::get_number()const{
	return CPlayer::m_number;
}


inline
const int CPlayer::get_position_points()const{
	return CPlayer::m_position_points;
}


inline
const int CPlayer::get_stone_count()const{
	return CPlayer::m_stone_count;
}

inline
const int CPlayer::get_teammate()const{
	return CPlayer::m_teammate;
}

inline
const int CPlayer::get_nemesis()const{
	return CPlayer::m_nemesis;
}

inline
void CPlayer::set_teammate(int playernumber){
	CPlayer::m_teammate = playernumber;
}

inline
void CPlayer::set_nemesis(int playernumber){
	CPlayer::m_nemesis = playernumber;
}



#endif

