#ifndef ___STONE____H__
#define ___STONE____H__

#include "constants.h"


/* Stone-Constants */

const int STONE_SIZE[STONE_COUNT_ALL_SHAPES] =
					{
						1, //0
						2, //1
						2, //2
						3, //3
						2, //4
						3, //5
						3, //6
						3, //7
						4, //8
						3, //9
						3, //10
						3, //11
						3, //12
						3, //13
						3, //14
						3, //15
						3, //16
						4, //17
						4, //18
						4, //19
						5  //20
					};


const int STONE_POSITION_POINTS[STONE_COUNT_ALL_SHAPES] = //testweise!!!!!!!!!!!!!!
					{
						8, //0
						4, //1
						2, //2
						4, //3
						8, //4
						1, //5
						2, //6
						2, //7
						4, //8
						1, //9
						2, //10
						2, //11
						2, //12
						2, //13
						1, //14
						1, //15
						8, //16
						1, //17
						1, //18
						1, //19
						4  //20
					};



const int STONE_POINTS[STONE_COUNT_ALL_SHAPES] =
					{
						1, //0
						2, //1
						3, //2
						3, //3
						4, //4
						4, //5
						4, //6
						4, //7
						4, //8
						5, //9
						5, //10
						5, //11
						5, //12
						5, //13
						5, //14
						5, //15
						5, //16
						5, //17
						5, //18
						5, //19
						5  //20
					};

const int STONE_MIRRORABLE[STONE_COUNT_ALL_SHAPES] =
					{
						MIRRORABLE_NOT,			//0
						MIRRORABLE_NOT,			//1
						MIRRORABLE_OPTIONAL,	//2
						MIRRORABLE_NOT,			//3
						MIRRORABLE_NOT,			//4
						MIRRORABLE_IMPORTANT,	//5
						MIRRORABLE_OPTIONAL,	//6
						MIRRORABLE_IMPORTANT,	//7
						MIRRORABLE_NOT,			//8
						MIRRORABLE_IMPORTANT,	//9
						MIRRORABLE_OPTIONAL,	//10
						MIRRORABLE_OPTIONAL,	//11
						MIRRORABLE_OPTIONAL,	//12
						MIRRORABLE_OPTIONAL,	//13
						MIRRORABLE_IMPORTANT,	//14
						MIRRORABLE_IMPORTANT,	//15
						MIRRORABLE_NOT,			//16
						MIRRORABLE_IMPORTANT,	//17
						MIRRORABLE_IMPORTANT,	//18
						MIRRORABLE_IMPORTANT,	//19
						MIRRORABLE_NOT			//20
					};

const int STONE_ROTATEABLE[STONE_COUNT_ALL_SHAPES] =
					{
						ROTATEABLE_NOT,		//0
						ROTATEABLE_TWO,		//1
						ROTATEABLE_FOUR,	//2
						ROTATEABLE_TWO,		//3
						ROTATEABLE_NOT,		//4
						ROTATEABLE_FOUR,	//5
						ROTATEABLE_FOUR,	//6
						ROTATEABLE_TWO,		//7
						ROTATEABLE_TWO,		//8
						ROTATEABLE_FOUR,	//9
						ROTATEABLE_FOUR,	//10
						ROTATEABLE_FOUR,	//11
						ROTATEABLE_FOUR,	//12
						ROTATEABLE_FOUR,	//13
						ROTATEABLE_TWO,		//14
						ROTATEABLE_FOUR,	//15
						ROTATEABLE_NOT,		//16
						ROTATEABLE_FOUR,	//17
						ROTATEABLE_FOUR,	//18
						ROTATEABLE_FOUR,	//19
						ROTATEABLE_TWO		//20
					};

const TStoneField STONE_FIELD[STONE_COUNT_ALL_SHAPES]=
					{
					  {1,8,8,8,8,	//0
						8,8,8,8,8,
						8,8,8,8,8,
						8,8,8,8,8,
						8,8,8,8,8,},

						{0,1,8,8,8,	//1
						0,1,8,8,8,
						8,8,8,8,8,
						8,8,8,8,8,
						8,8,8,8,8,},

						{1,0,8,8,8,	//2
						1,1,8,8,8,
						8,8,8,8,8,
						8,8,8,8,8,
						8,8,8,8,8,},

						{0,1,0,8,8,	//3
						0,2,0,8,8,
						0,1,0,8,8,
						8,8,8,8,8,
						8,8,8,8,8,},

						{1,1,8,8,8,	//4
						1,1,8,8,8,
						8,8,8,8,8,
						8,8,8,8,8,
						8,8,8,8,8,},

						{0,1,0,8,8,	//5
						0,2,0,8,8,
						1,1,0,8,8,
						8,8,8,8,8,
						8,8,8,8,8,},

						{0,1,0,8,8,	//6
						0,2,1,8,8,
						0,1,0,8,8,
						8,8,8,8,8,
						8,8,8,8,8,},

						{0,0,0,8,8,	//7
						1,1,0,8,8,
						0,1,1,8,8,
						8,8,8,8,8,
						8,8,8,8,8,},

						{0,0,1,0,8,	//8
						0,0,2,0,8,
						0,0,2,0,8,
						0,0,1,0,8,
						8,8,8,8,8,},

						{0,1,0,8,8,	//9
						1,2,0,8,8,
						1,1,0,8,8,
						8,8,8,8,8,
						8,8,8,8,8,},

						{1,1,0,8,8,	//10
						0,2,0,8,8,
						1,1,0,8,8,
						8,8,8,8,8,
						8,8,8,8,8,},

						{0,1,0,8,8,	//11
						0,2,0,8,8,
						1,2,1,8,8,
						8,8,8,8,8,
						8,8,8,8,8,},

						{1,0,0,8,8,	//12
						2,0,0,8,8,
						1,2,1,8,8,
						8,8,8,8,8,
						8,8,8,8,8,},

						{1,1,0,8,8,	//13
						0,1,1,8,8,
						0,0,1,8,8,
						8,8,8,8,8,
						8,8,8,8,8,},

						{1,0,0,8,8,	//14
						1,2,1,8,8,
						0,0,1,8,8,
						8,8,8,8,8,
						8,8,8,8,8,},

						{1,0,0,8,8,	//15
						1,2,1,8,8,
						0,1,0,8,8,
						8,8,8,8,8,
						8,8,8,8,8,},

						{0,1,0,8,8,	//16
						1,2,1,8,8,
						0,1,0,8,8,
						8,8,8,8,8,
						8,8,8,8,8,},

						{0,0,1,0,8,	//17
						0,0,2,0,8,
						0,0,2,0,8,
						0,1,1,0,8,
						8,8,8,8,8,},

						{0,0,1,0,8,	//18
						0,0,2,0,8,
						0,1,1,0,8,
						0,1,0,0,8,
						8,8,8,8,8,},

						{0,1,0,0,8,	//19
						0,2,1,0,8,
						0,2,0,0,8,
						0,1,0,0,8,
						8,8,8,8,8,},

						{0,0,1,0,0,	//20
						0,0,2,0,0,
						0,0,2,0,0,
						0,0,2,0,0,
						0,0,1,0,0},
					};


/* ende Stone-Constants */












class CSpiel;

class CStone{
	private:
		int m_available;
		int m_shape;
		int m_size;
		int m_mirror_counter;
		int m_rotate_counter;

		const bool is_position_inside_stone(const int y, const int x)const; //braucht get_stone_field

	public:
		CStone():m_available(0),m_shape(0),m_mirror_counter(0),m_rotate_counter(0) {}
		void init (const int shape);

		const TSingleStone get_stone_field(const int y, const int x)const;

		const int calculate_possible_turns_in_position(const CSpiel* const spiel, const int playernumber, const int fieldY, const int fieldX);

		const int get_stone_size()const;
		const int get_stone_points()const;
		const int get_stone_shape()const;
		const int get_number()const; // = stone_shape
		const int get_rotateable()const; //siehe constants.h für die Rückgabe
		const int get_mirrorable()const; //siehe constants.h für die Rückgabe
		const int get_rotate_counter()const;
		const int get_mirror_counter()const;
		const int get_stone_position_points()const;

		const int get_available()const;
		void set_available(const int value);
		void available_decrement();
		void available_increment();

		void rotate_left();
		void rotate_right();
		void mirror_over_x();
		void mirror_over_y();
		void mirror_rotate_to(const int mirror_counter, const int rotate_counter);

};


inline
void CStone::mirror_rotate_to(const int mirror_counter, const int rotate_counter){
	CStone::m_mirror_counter = mirror_counter;
	CStone::m_rotate_counter = rotate_counter;
}

inline
void CStone::set_available(const int value){
	m_available = value;
}

inline
void CStone::available_increment(){
	m_available++;
}

inline
void CStone::available_decrement(){
	m_available--;
}

inline
const int CStone::get_available()const{
	return CStone::m_available;
}


inline
const int CStone::get_mirrorable()const{
	return STONE_MIRRORABLE[m_shape];
}


inline
const int CStone::get_rotateable()const{
	return STONE_ROTATEABLE[m_shape];
}

inline
const int CStone::get_stone_position_points()const{
	return STONE_POSITION_POINTS[m_shape];
}


inline
const int CStone::get_rotate_counter()const{
	return CStone::m_rotate_counter;
}


inline
const int CStone::get_mirror_counter()const{
	return CStone::m_mirror_counter;
}

inline
const int CStone::get_stone_shape()const{
	return CStone::m_shape;
}

inline
const int CStone::get_number()const{
	return CStone::m_shape;
}

inline
const int CStone::get_stone_size()const{
	return m_size;
}

inline
const int CStone::get_stone_points()const{
	return STONE_POINTS[m_shape];
}

inline
const bool CStone::is_position_inside_stone(const int y,const int x)const{
	if (y < 0 || y >= m_size || x < 0 || x >= m_size) return false;
	return true;
}




#endif
