package de.saschahlusiak.freebloks.model;

public class Turnpool {
	public Turn m_tail;
	Turn m_head;
	Turn m_current;

	public void add_turn(Turn turn) {
		if (null == m_head) {
			Turn new_element = new Turn(turn);
			new_element.m_turn_number = 1;
			m_head = new_element;
			m_tail = new_element;
		} else {
			if (m_current != null) {
				m_tail = m_current;
				m_current.copy(turn);
				m_current = m_current.m_next;
			} else {
				Turn new_element = new Turn(turn);
				new_element.m_turn_number = m_tail.m_turn_number + 1;
				m_tail.m_next = new_element;
				m_tail = new_element;
			}
		}
	}

	public void add_turn(int playernumber, Stone stone, int y, int x) {
		if (null == m_head){
			Turn new_element = new Turn(playernumber, stone, y, x);
			new_element.m_turn_number = 1;
			m_head = new_element;
			m_tail = new_element;
		} else {
			if (m_current != null) {
				m_tail = m_current;
				m_current.init(playernumber, stone, y, x);
				m_current = m_current.m_next;
			} else {
				Turn new_element = new Turn(playernumber, stone, y, x);
				new_element.m_turn_number = m_tail.m_turn_number + 1;;
				m_tail.m_next = new_element;
				m_tail = new_element;
			}
		}
	}
	
	void begin_add() {
		m_current = m_tail = m_head;
	}

	void end_add() {
		if (m_current == m_head) {
			m_head = m_tail = null;		
		} else {
			m_tail.m_next = null;
		}
		m_current = m_head;
	}

	public void delete_all_turns() {
		m_tail = null;
		m_head = null;
		m_current = null;
	}

	void delete_last() {
		if (get_number_of_stored_turns() == 1) {
			delete_all_turns();
			return;
		}
			
		m_current = m_head;
		while (m_current.m_next != m_tail) {
			m_current = m_current.m_next;
		}
		m_current.m_next = null;
		m_tail = m_current;
	}


	Turn get_turn(int i) {
		if (i < m_current.m_turn_number) m_current = m_head;
		while (i > m_current.m_turn_number){
			m_current = m_current.m_next;
		}
		return m_current;	
	}
	
	int get_number_of_stored_turns() {
		if (m_tail == null)
			return 0;
		return m_tail.m_turn_number;
	}
}
