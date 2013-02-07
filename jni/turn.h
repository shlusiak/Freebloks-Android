#ifndef ___TURN____H_
#define ___TURN____H_

#include "stone.h"

//#include "spiel.h"

class CSpiel;
class CPlayer;

class CTurn{

	private:
		int m_playernumber;

		int m_stone_number;
		
		int m_mirror_count;
		int m_rotate_count;

		int m_y;
		int m_x;

		int m_turn_number;

		//zeiger auf nachfolger
		CTurn* m_next;
	public:
		
		CTurn(const CTurn* turn);
		CTurn(const int playernumber, const CStone* stone, const int y, const int x);

		virtual ~CTurn();

		void init_CTurn(const CTurn* turn);
		void init_CTurn(const int playernumber, const CStone* stone, const int y, const int x);
 
		
		
		const int get_stone_number()const;
		const int get_turn_number()const;
		
		const int get_mirror_count()const;
		const int get_rotate_count()const;

		const int get_y()const;
		const int get_x()const;

		const int get_playernumber()const;

		CTurn* get_next()const;
						
		void set_next(CTurn* next_turn);
		void set_number(int number);
		
		
		
};



inline
const int CTurn::get_x()const{
	return CTurn::m_x;
}

		
inline
const int CTurn::get_y()const{
	return CTurn::m_y;
}


inline
const int CTurn::get_stone_number()const{
	return CTurn::m_stone_number;
}


inline
const int CTurn::get_turn_number() const{
	return CTurn::m_turn_number;
}


inline
const int CTurn::get_rotate_count()const{
	return CTurn::m_rotate_count;
}


inline
const int CTurn::get_mirror_count()const{
	return CTurn::m_mirror_count;
}


inline
CTurn* CTurn::get_next()const{
	return CTurn::m_next;
}

inline
const int CTurn::get_playernumber()const{
	return CTurn::m_playernumber;
}

inline
void CTurn::set_next(CTurn* next_turn){
	CTurn::m_next = next_turn;
}

inline
void CTurn::set_number(int number){
	CTurn::m_turn_number = number;
}





#endif


