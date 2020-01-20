#ifdef HAVE_CONFIG_H
  #include "config.h"
#endif

#include "turn.h"

#include "spiel.h"


CTurn::CTurn(const CTurn* turn) {
	CTurn::m_next = NULL;
	init_CTurn(turn);
}

CTurn::CTurn(const int playernumber, const CStone* stone, const int y, const int x) {
	CTurn::m_next = NULL;
	init_CTurn(playernumber, stone, y, x);
}

CTurn::~CTurn(){
	if (m_next){
		delete m_next;
	}
}

void CTurn::init_CTurn(const CTurn* turn) {
	m_playernumber = turn->get_playernumber();
	m_stone_number = turn->get_stone_number();
	m_mirror_count = turn->get_mirror_count();
	m_rotate_count = turn->get_rotate_count();
	m_y = turn->get_y();
	m_x = turn->get_x();
}

void CTurn::init_CTurn(const int playernumber, const CStone* stone, const int y, const int x){
	m_playernumber = playernumber;
	m_stone_number = stone->get_number();
	m_mirror_count = stone->get_mirror_counter();
	m_rotate_count = stone->get_rotate_counter();
	m_y = y;
	m_x = x;
}

