#ifdef HAVE_CONFIG_H
  #include "config.h"
#endif

#include "stone.h"
#include "spiel.h"



void CStone::init(const int shape){	
	CStone::m_available = 1;
	CStone::m_shape = shape;
	CStone::m_size = STONE_SIZE[CStone::m_shape];
	CStone::m_rotate_counter = 0;
	CStone::m_mirror_counter = 0;
}


const TSingleStone CStone::get_stone_field(const int y, const int x)const{
	#ifdef _DEBUG
		if (!is_position_inside_stone(y,x)) error_exit("Stone field mit is_position_inside_stone �berpr�fen!!", 23);
	#endif
	int nx=x,ny=y;
	if (CStone::m_mirror_counter == 0){
		if (CStone::m_rotate_counter == 0){
			nx = y;
			ny = x;
		} else if (CStone::m_rotate_counter == 1){
			nx = m_size-1-x;
			ny = y;
		} else if (CStone::m_rotate_counter == 2){
			nx = m_size-1-y;
			ny = m_size-1-x;
		} else if (CStone::m_rotate_counter == 3){
			nx = x;
			ny = m_size-1-y;
		} else error_exit("unbekannter steinzustand!", 15); //debug
	}else{
		if (CStone::m_rotate_counter == 0){
			nx = m_size-1-y;
			ny = x;
		} else if (CStone::m_rotate_counter == 1){
			nx = x;
			ny = y;
		} else if (CStone::m_rotate_counter == 2){
			nx = y;
			ny = m_size-1-x;
		} else
		if (CStone::m_rotate_counter == 3){
			nx = m_size-1-x;
			ny = m_size-1-y;
		} else error_exit("unbekannter steinzustand!", 15); //debug
	}
	
	return STONE_FIELD[CStone::m_shape][nx][ny];
}



void CStone::rotate_left(){
	CStone::m_rotate_counter--;
	if (CStone::m_rotate_counter < 0) CStone::m_rotate_counter += STONE_ROTATEABLE[m_shape];
}

void CStone::rotate_right(){
	CStone::m_rotate_counter=(CStone::m_rotate_counter+1)%STONE_ROTATEABLE[m_shape];
// 	if (CStone::m_rotate_counter >= STONE_ROTATEABLE[m_shape]) CStone::m_rotate_counter = 0; 
}

void CStone::mirror_over_x(){
	if (STONE_ROTATEABLE[m_shape] == MIRRORABLE_NOT) return;
	CStone::m_mirror_counter = (CStone::m_mirror_counter + 1) % 2;
	if (m_rotate_counter%2 == 1) 
		CStone::m_rotate_counter = (CStone::m_rotate_counter + 2)%(STONE_ROTATEABLE[CStone::m_shape]);
}

 void CStone::mirror_over_y(){
	if (STONE_ROTATEABLE[m_shape] == MIRRORABLE_NOT) return;
	CStone::m_mirror_counter = (CStone::m_mirror_counter + 1) % 2;
	if (CStone::m_rotate_counter%2 == 0) 
		CStone::m_rotate_counter = (CStone::m_rotate_counter + 2)%(STONE_ROTATEABLE[CStone::m_shape]);
}




const int CStone::calculate_possible_turns_in_position(const CSpiel* spiel, const int playernumber, const int fieldY, const int fieldX){
	int mirror;
	int count = 0;
	
	const int rotate_count = CStone::m_rotate_counter;///debug siehe unten
	const int mirror_count = CStone::m_mirror_counter;///debug siehe unten

	if (STONE_MIRRORABLE[CStone::m_shape] == MIRRORABLE_IMPORTANT) mirror = 1;
	else mirror = 0;

	for (CStone::m_mirror_counter = 0; CStone::m_mirror_counter <= mirror; CStone::m_mirror_counter++){
		for (CStone::m_rotate_counter = 0; CStone::m_rotate_counter < STONE_ROTATEABLE[CStone::m_shape]; CStone::m_rotate_counter++){
			
			for (int x = 0; x < STONE_SIZE[CStone::m_shape]; x++){
				for (int y = 0; y < STONE_SIZE[CStone::m_shape]; y++){
					
					if (CStone::get_stone_field(y, x) == STONE_FIELD_ALLOWED) {  //es wird get_stone_field benutzt, da gedreht wurde      					
						if (spiel->is_valid_turn(this, playernumber, fieldY-y, fieldX-x) == FIELD_ALLOWED){
							count++;
						}
					}
				}
			}
		}
	}
	CStone::m_rotate_counter = rotate_count;
	CStone::m_mirror_counter = mirror_count;
	return count;
}
