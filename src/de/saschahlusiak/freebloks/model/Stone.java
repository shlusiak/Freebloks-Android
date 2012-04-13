package de.saschahlusiak.freebloks.model;

public class Stone {
	static final int STONE_COUNT_ALL_SHAPES = 21;
	static final int STONE_SIZE_MAX  =  5;

	static final int MIRRORABLE_NOT = 0;
	static final int MIRRORABLE_OPTIONAL = 1;
	static final int MIRRORABLE_IMPORTANT = 2;

	static final int ROTATEABLE_NOT   = 1;
	static final int ROTATEABLE_TWO   = 2;
	static final int ROTATEABLE_FOUR  = 4;
	
	static final int FIELD_FREE   =  240;
	static final int FIELD_ALLOWED=  241;
	static final int FIELD_DENIED =  255;

	static final int STONE_FIELD_FREE = 0;
	static final int STONE_FIELD_ALLOWED = 1;

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
	int m_mirror_counter;
	int m_rotate_counter;

	
	public Stone() {
		m_available = m_shape = m_mirror_counter = m_rotate_counter = 0;
	}
	
	public void mirror_rotate_to(int mirror_counter, int rotate_counter){
		this.m_mirror_counter = mirror_counter;
		this.m_rotate_counter = rotate_counter;
	}

	public void set_available(int value){
		this.m_available = value;
	}

	public void available_increment(){
		m_available++;
	}

	public void available_decrement(){
		m_available--;
	}

	public int get_available() {
		return m_available;
	}

	public int get_mirrorable() {
		return STONE_MIRRORABLE[m_shape];
	}

	public int get_rotateable() {
		return STONE_ROTATEABLE[m_shape];
	}

	public int get_stone_position_points() {
		return STONE_POSITION_POINTS[m_shape];
	}

	public int get_rotate_counter() {
		return m_rotate_counter;
	}

	public int get_mirror_counter() {
		return m_mirror_counter;
	}

	public int get_stone_shape() {
		return m_shape;
	}

	
	public int get_number() {
		return m_shape;
	}

	
	public int get_stone_size() {
		return m_size;
	}

	
	public int get_stone_points() {
		return STONE_POINTS[m_shape];
	}

	
	public boolean is_position_inside_stone(int y, int x) { 
		if (y < 0 || y >= m_size || x < 0 || x >= m_size) return false;
		return true;
	}

	public void CStone(int shape){
		init(shape);
	}
	
	void init(int shape) {
		m_available = 1;
		m_shape = shape;
		m_size = STONE_SIZE[m_shape];
		m_rotate_counter = 0;
		m_mirror_counter = 0;		
	}


	public int get_stone_field(int y, int x) {
		int nx=x,ny=y;
		if (m_mirror_counter == 0){
			if (m_rotate_counter == 0){
				nx = y;
				ny = x;
			} else if (m_rotate_counter == 1){
				nx = m_size-1-x;
				ny = y;
			} else if (m_rotate_counter == 2){
				nx = m_size-1-y;
				ny = m_size-1-x;
			} else if (m_rotate_counter == 3){
				nx = x;
				ny = m_size-1-y;
			} else return 0; /* ERROR */
		}else{
			if (m_rotate_counter == 0){
				nx = m_size-1-y;
				ny = x;
			} else if (m_rotate_counter == 1){
				nx = x;
				ny = y;
			} else if (m_rotate_counter == 2){
				nx = y;
				ny = m_size-1-x;
			} else
			if (m_rotate_counter == 3){
				nx = m_size-1-x;
				ny = m_size-1-y;
			} else return 0; /* ERROR */
		}
		
		return STONE_FIELD[m_shape][nx + ny * STONE_SIZE_MAX];
	}



	void rotate_left(){
		m_rotate_counter--;
		if (m_rotate_counter < 0) m_rotate_counter += STONE_ROTATEABLE[m_shape];
	}

	void rotate_right(){
		m_rotate_counter=(m_rotate_counter+1)%STONE_ROTATEABLE[m_shape];
	}

	void mirror_over_x(){
		if (STONE_ROTATEABLE[m_shape] == MIRRORABLE_NOT) return;
		m_mirror_counter = (m_mirror_counter + 1) % 2;
		if (m_rotate_counter%2 == 1) 
			m_rotate_counter = (m_rotate_counter + 2)%(STONE_ROTATEABLE[m_shape]);
	}

	 void mirror_over_y(){
		if (STONE_ROTATEABLE[m_shape] == MIRRORABLE_NOT) return;
		m_mirror_counter = (m_mirror_counter + 1) % 2;
		if (m_rotate_counter%2 == 0) 
			m_rotate_counter = (m_rotate_counter + 2)%(STONE_ROTATEABLE[m_shape]);
	}

	int calculate_possible_turns_in_position(Spiel spiel, int playernumber, int fieldY, int fieldX){
		int mirror;
		int count = 0;
		
		int rotate_count = m_rotate_counter;
		int mirror_count = m_mirror_counter;

		if (STONE_MIRRORABLE[m_shape] == MIRRORABLE_IMPORTANT) mirror = 1;
		else mirror = 0;

		for (m_mirror_counter = 0; m_mirror_counter <= mirror; m_mirror_counter++){
			for (m_rotate_counter = 0; m_rotate_counter < STONE_ROTATEABLE[m_shape]; m_rotate_counter++){
				
				for (int x = 0; x < STONE_SIZE[m_shape]; x++){
					for (int y = 0; y < STONE_SIZE[m_shape]; y++){
						
						if (get_stone_field(y, x) == STONE_FIELD_ALLOWED) {      					
							if (spiel.is_valid_turn(this, playernumber, fieldY-y, fieldX-x) == FIELD_ALLOWED){
								count++;
							}
						}
					}
				}
			}
		}
		m_rotate_counter = rotate_count;
		m_mirror_counter = mirror_count;
		return count;
	}

}
