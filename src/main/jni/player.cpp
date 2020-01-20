#ifdef HAVE_CONFIG_H
  #include "config.h"
#endif

#include "player.h"

#include "spiel.h"




void CPlayer::init(const CSpiel* spiel, const int playernumber){
	m_number = playernumber;
	for (int i = 0; i < STONE_COUNT_ALL_SHAPES; i++){
		m_stone[i].init(i);
	}
	refresh_data(spiel);
}

void CPlayer::refresh_data(const CSpiel* spiel){

	m_stone_points_left = 0;
	m_number_of_possible_turns = 0;
	m_position_points = 0;
	m_stone_count = 0;

	for (int n = 0; n < STONE_COUNT_ALL_SHAPES; n++){
		CStone* stone = &CPlayer::m_stone[n];
		m_stone_count += stone->get_available();
		m_stone_points_left += stone->get_stone_points() * stone->get_available();
	}

	for (int x = 0; x < spiel->get_field_size_x(); x++){
		for (int y = 0; y < spiel->get_field_size_y(); y++){
			if (spiel->get_game_field(CPlayer::m_number, y, x) == FIELD_ALLOWED){
				for (int n = 0; n < STONE_COUNT_ALL_SHAPES; n++){
					CStone* stone = &CPlayer::m_stone[n];
					if (stone->get_available()){
						int pos_turns;

						pos_turns = stone->calculate_possible_turns_in_position(spiel, CPlayer::m_number, y, x);
						CPlayer::m_number_of_possible_turns += pos_turns;
						CPlayer::m_position_points += pos_turns * stone->get_stone_position_points() * stone->get_stone_points(); //ist ein guter wert!!!!
					}
				}
			}
		}
	}
}



