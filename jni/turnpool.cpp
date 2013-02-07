#ifdef HAVE_CONFIG_H
  #include "config.h"
#endif

#include "turnpool.h"


//add_turn sind fast identisch
void CTurnpool::add_turn(const CTurn* turn){
	if (NULL == m_head){
		CTurn* new_element = new CTurn(turn);
		new_element->set_number(1);
		m_head = new_element;
		m_tail = new_element;
	}else{
		if (m_current) {
			m_tail = m_current;
			m_current->init_CTurn(turn);
			m_current = m_current->get_next();
		} else {
			CTurn* new_element = new CTurn(turn);
			new_element->set_number(m_tail->get_turn_number()+1);
			m_tail->set_next(new_element);
			m_tail = new_element;
		}
	}
}

void CTurnpool::add_turn(const int playernumber, const CStone* stone, const int y, const int x){
	if (NULL == m_head){
		CTurn* new_element = new CTurn(playernumber, stone, y, x);
		new_element->set_number(1);
		m_head = new_element;
		m_tail = new_element;
	}else{
		if (m_current) {
			m_tail = m_current;
			m_current->init_CTurn(playernumber, stone, y, x);
			m_current = m_current->get_next();
		} else {
			CTurn* new_element = new CTurn(playernumber, stone, y, x);
			new_element->set_number(m_tail->get_turn_number()+1);
			m_tail->set_next(new_element);
			m_tail = new_element;
		}
	}
}

void CTurnpool::end_add() {
	if (m_current == m_head) {
		if (m_head) delete m_head;
		m_head = m_tail = NULL;		
	} else if (m_tail->get_next()) {
		delete m_tail->get_next();
		m_tail->set_next(NULL);
	}
	m_current = m_head;
}

void CTurnpool::delete_all_turns(){
	if (m_head) { 
		delete m_head;
	}
	m_tail = NULL;
	m_head = NULL;
	m_current = NULL;
}

void CTurnpool::delete_last(){
	#ifdef _DEBUG
		if (CTurnpool::get_number_of_stored_turns() == 0) error_exit("Turnpool ist leer!!... delete_last unsinnig", 31);
	#endif
	
	if (CTurnpool::get_number_of_stored_turns() == 1) {
		CTurnpool::delete_all_turns();
		return;
	}
		
	m_current = m_head;
	while (m_current->get_next() != m_tail){
		m_current = m_current->get_next();
	}
	m_current->set_next(NULL);
	delete m_tail;
	m_tail = m_current;
}


CTurn* CTurnpool::get_turn(int i) {
	#ifdef _DEBUG
		if (0 == m_head || i > m_tail->get_turn_number()) error_exit("Turnpool ist leer. Wertrückgabe nicht möglich!", 6); //debug
	#endif
	if (i < m_current->get_turn_number()) m_current = m_head;
	while (i > m_current->get_turn_number()){
		m_current = m_current->get_next();
	}
	return m_current;	
}

