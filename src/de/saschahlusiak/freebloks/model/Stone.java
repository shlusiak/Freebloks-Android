package de.saschahlusiak.freebloks.model;

import java.io.Serializable;

import de.saschahlusiak.freebloks.controller.GameStateException;

public class Stone implements Serializable, Cloneable {
	private static final long serialVersionUID = -4949247356899826370L;

	public static final int STONE_COUNT_ALL_SHAPES = 21;
	public static final int STONE_SIZE_MAX  =  5;

	public static final int MIRRORABLE_NOT = 0;
	public static final int MIRRORABLE_OPTIONAL = 1;
	public static final int MIRRORABLE_IMPORTANT = 2;

	public static final int ROTATEABLE_NOT   = 1;
	public static final int ROTATEABLE_TWO   = 2;
	public static final int ROTATEABLE_FOUR  = 4;

	public static final int FIELD_FREE   =  240;
	public static final int FIELD_ALLOWED=  241;
	public static final int FIELD_DENIED =  255;

	public static final int STONE_FIELD_FREE = 0;
	public static final int STONE_FIELD_ALLOWED = 1;

	static final int STONE_SIZE[] =
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


	static final int STONE_POSITION_POINTS[] =
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


	static final int STONE_POINTS[] =
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

	static final int STONE_MIRRORABLE[] =
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

	static final int STONE_ROTATEABLE[] =
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

	static final int STONE_FIELD[][] =
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

	int m_available;
	int m_shape;
	int m_size;


	public Stone() {
		m_available = m_shape = m_size = 0;
	}
	
	public Stone(int shape) {
		init(shape);
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public final void copyFrom(Stone from) {
		this.m_available = from.m_available;
		this.m_shape = from.m_shape;
		this.m_size = from.m_size;
	}

	public final void set_available(int value){
		this.m_available = value;
	}

	public final void available_increment(){
		m_available++;
	}

	public final void available_decrement() throws GameStateException {
		if (m_available <= 0)
			throw new GameStateException("stone not available");
		m_available--;
	}

	public final int get_available() {
		return m_available;
	}

	public final int get_mirrorable() {
		return STONE_MIRRORABLE[m_shape];
	}

	public final int get_rotateable() {
		return STONE_ROTATEABLE[m_shape];
	}

	public final int get_stone_position_points() {
		return STONE_POSITION_POINTS[m_shape];
	}

	public final int get_stone_shape() {
		return m_shape;
	}

	public final int get_number() {
		return m_shape;
	}

	public final int get_stone_size() {
		return m_size;
	}

	public final int get_stone_points() {
		return STONE_POINTS[m_shape];
	}

	public final void init(int shape) {
		m_shape = shape;
		m_size = STONE_SIZE[m_shape];
	}

	public final int get_stone_field(int y, int x, int mirror, int rotate) {
		int nx=x, ny=y;
		if (mirror == 0){
			if (rotate == 0){
				nx = y;
				ny = x;
			} else if (rotate == 1){
				nx = m_size - 1 - x;
				ny = y;
			} else if (rotate == 2){
				nx = m_size - 1 - y;
				ny = m_size - 1 - x;
			} else if (rotate == 3){
				nx = x;
				ny = m_size - 1 - y;
			} else throw new RuntimeException("invalid m_rotate_counter: " + rotate);
		}else{
			if (rotate == 0){
				nx = m_size - 1 - y;
				ny = x;
			} else if (rotate == 1){
				nx = x;
				ny = y;
			} else if (rotate == 2){
				nx = y;
				ny = m_size - 1 - x;
			} else
			if (rotate == 3){
				nx = m_size - 1 - x;
				ny = m_size - 1 - y;
			} else throw new RuntimeException("invalid m_rotate_counter: " + rotate);
		}

		return STONE_FIELD[m_shape][ny + nx * STONE_SIZE_MAX];
	}

	final int calculate_possible_turns_in_position(Spiel spiel, int playernumber, int fieldY, int fieldX) {
		int count = 0;

		int mirror, mirror_max;
		int rotate;

		if (STONE_MIRRORABLE[m_shape] == MIRRORABLE_IMPORTANT)
			mirror_max = 1;
		else 
			mirror_max = 0;

		for (mirror = 0; mirror <= mirror_max; mirror++) {
			for (rotate = 0; rotate < STONE_ROTATEABLE[m_shape]; rotate++) {
				for (int x = 0; x < STONE_SIZE[m_shape]; x++) {
					for (int y = 0; y < STONE_SIZE[m_shape]; y++) {
						if (get_stone_field(y, x, mirror, rotate) == STONE_FIELD_ALLOWED) {
							if (spiel.is_valid_turn(this, playernumber, fieldY - y, fieldX - x, mirror, rotate) == FIELD_ALLOWED) {
								count++;
							}
						}
					}
				}
			}
		}
		
		return count;
	}
}
