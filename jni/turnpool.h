#ifndef ___TURNPOOL_H____
#define ___TURNPOOL_H____

#include <stdlib.h>
#include <stdio.h>
#include "turn.h"



class CTurnpool{
	private:
		
		CTurn *m_tail;
		CTurn *m_head;
		CTurn *m_current;

	public: 
		CTurnpool():m_tail(0),m_head(0),m_current(0) {};
		~CTurnpool() { delete_all_turns(); }

		void add_turn(const int playernumber, const CStone* stone, const int y, const int x);
		void add_turn(const CTurn* turn);
		void delete_all_turns();
		
		void begin_add() { m_current = m_tail = m_head; }
		void end_add(); 

		const int get_number_of_stored_turns()const;
		CTurn* get_turn(int i);
		CTurn* get_last_turn()const { return m_tail; }
		void delete_last();
};

inline const int CTurnpool::get_number_of_stored_turns()const{
	if (NULL == m_tail) return 0;
	return m_tail->get_turn_number();
}
 








#endif

