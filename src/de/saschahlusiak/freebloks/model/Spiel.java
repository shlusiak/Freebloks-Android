package de.saschahlusiak.freebloks.model;

public class Spiel {
	public static final int PLAYER_MAX = 4;
	public static final int DEFAULT_FIELD_SIZE_X = 20;
	public static final int DEFAULT_FIELD_SIZE_Y = 20;
	
	static final int PLAYER_BIT_ADDR[] = {
			  3, //0
			 12, //1
			 48, //2
			192  //3
		};

	static final int PLAYER_BIT_ALLOWED[] = {
			  1, //0
			  4, //1
			 16, //2
			 64  //3
		};

	static final int PLAYER_BIT_DENIED[] = {
			  2, //0
			  8, //1
			 32, //2
			128  //3
		};

	static final int PLAYER_BIT_HAVE_MIN = 252;
	
	protected int m_field_size_y;
	protected int m_field_size_x;

	Player m_player[] = new Player[PLAYER_MAX];
	int m_game_field[][];
	
	public Spiel(int size_y, int size_x) {
		m_game_field = null;
		m_field_size_y = size_y;
		m_field_size_x = size_x;
		for (int i = 0; i < PLAYER_MAX; i++)
			m_player[i] = new Player();
		init_field();
	}
	
	public Spiel(Spiel s) {
		m_game_field = null;
		m_field_size_y = s.m_field_size_y;
		m_field_size_x = s.m_field_size_x;
		
		m_game_field = new int[m_field_size_y][m_field_size_x];
		for (int i = 0; i < PLAYER_MAX; i++)
			m_player[i] = new Player();
	}
	
	private void init_field() {
		m_game_field = new int[m_field_size_y][m_field_size_x];
		for (int y = 0; y < m_field_size_y; y++) {
			for (int x = 0; x < m_field_size_x ; x++){
				m_game_field[y][x] = 0;
			}
		}
		for (int p = 0; p < PLAYER_MAX; p++){
			set_game_field(get_player_start_y(p), get_player_start_x(p), PLAYER_BIT_ALLOWED[p]);
		}
	}
	
	void follow_situation(int vorher_playernumber, Spiel vorher_situation, Turn turn) {
		int i,j;
		for (i = 0; i < m_field_size_x; i++)
			for (j = 0; j < m_field_size_y; j++)
				m_game_field[j][i] = vorher_situation.m_game_field[j][i];
		
		for (i = 0; i < PLAYER_MAX; i++) {
			m_player[i].copyFrom(vorher_situation.m_player[i]);
		}
		
		set_stone(turn);
	}
	

	
	int get_game_field_value(int y, int x) {
		return m_game_field[y][x];
	}

	int get_game_field(int playernumber, int y, int x) {
		int wert = get_game_field_value(y, x);
		if (wert >= PLAYER_BIT_HAVE_MIN) return Stone.FIELD_DENIED;
		wert &= PLAYER_BIT_ADDR[playernumber];
		if (wert == 0) return Stone.FIELD_FREE;
		if (wert > PLAYER_BIT_ALLOWED[playernumber]) return Stone.FIELD_DENIED;
		return Stone.FIELD_ALLOWED;
	}

	int get_game_field(int y, int x) {
		int wert = get_game_field_value(y,x);
		if (wert < PLAYER_BIT_HAVE_MIN) return Stone.FIELD_FREE;
		return wert & 3;
	}

	void set_game_field(int y, int x, int value){
		m_game_field[y][x] = value;
	}

	public Player get_player(int playernumber) {
		return m_player[playernumber];
	}

	boolean is_position_inside_field(int y, int x) {
		return (y >= 0 && y < m_field_size_y && x >= 0 && x < m_field_size_x);
	}

	int get_max_stone_size() {
		return Stone.STONE_SIZE_MAX;
	}

	int get_player_start_x(int playernumber) {
		switch (playernumber) {
		case 0 : 
		case 1 : return 0;
		default: return m_field_size_x - 1;
		}
	}

	int get_player_start_y(int playernumber) {
		switch (playernumber){
		case 1 :
		case 2 : return 0;
		default: return m_field_size_y - 1;
		}

	}
	
	protected void set_field_size_and_new(int y, int x) {
		m_field_size_x = x;
		m_field_size_y = y;
		m_game_field = new int[m_field_size_y][m_field_size_x];
		start_new_game();
	}


	public void set_stone_numbers(int einer, int zweier, int dreier, int vierer, int fuenfer){
		int counts[] = {einer, zweier, dreier, vierer, fuenfer};

		for (int n = 0 ; n < Stone.STONE_COUNT_ALL_SHAPES; n++){  
			for (int p = 0; p < PLAYER_MAX; p++){
				Stone stone = m_player[p].get_stone(n);
				stone.set_available(counts[stone.get_stone_points() - 1]);
			}
		}

		refresh_player_data();
	}


	protected void set_teams(int player_team1_1, int player_team1_2, int player_team2_1, int player_team2_2){
		m_player[player_team1_1].set_teammate(player_team1_2);
		m_player[player_team1_2].set_teammate(player_team1_1);
		m_player[player_team1_1].set_nemesis(player_team2_1);
		m_player[player_team1_2].set_nemesis(player_team2_1);
		
		m_player[player_team2_1].set_teammate(player_team2_2);
		m_player[player_team2_2].set_teammate(player_team2_1);
		m_player[player_team2_1].set_nemesis(player_team1_1);
		m_player[player_team2_2].set_nemesis(player_team1_1);
	}

	public void start_new_game(){
		for (int n = 0; n < PLAYER_MAX; n++){
			m_player[n].init(this, n);
		}
	} 

	void refresh_player_data(){
		for (int n = 0; n < PLAYER_MAX; n++){
			m_player[n].refresh_data(this);
		}
	}


	protected int is_valid_turn(Stone stone, int playernumber, int startY, int startX) {
		int valid = Stone.FIELD_DENIED;
		int field_value;

		for (int y = 0; y < stone.get_stone_size(); y++){
			for (int x = 0; x < stone.get_stone_size(); x++){
				if (stone.get_stone_field(y,x) != Stone.STONE_FIELD_FREE) {
					if (!is_position_inside_field(y + startY, x + startX)) return Stone.FIELD_DENIED;

					field_value = get_game_field (playernumber, y + startY , x + startX);
					if (field_value == Stone.FIELD_DENIED) return Stone.FIELD_DENIED;
					if (field_value == Stone.FIELD_ALLOWED) valid = Stone.FIELD_ALLOWED;
				}
			}
		}
		return valid;
	}

	int is_valid_turn(Turn turn){
		int playernumber = turn.m_playernumber;
		Stone stone = m_player[playernumber].get_stone(turn.m_stone_number);
		stone.mirror_rotate_to(turn.m_mirror_count, turn.m_rotate_count);
		return is_valid_turn(stone, playernumber, turn.m_y, turn.m_x);
	}

	void free_gamefield(int y, int x){
		set_game_field(y, x, 0);
	}

	void set_single_stone_for_player(int playernumber, int startY, int startX){
		set_game_field(startY , startX, PLAYER_BIT_HAVE_MIN | playernumber);
		for (int y = startY-1; y <= startY+1; y++)if (y>=0 && y<m_field_size_y) {
			for (int x = startX-1; x <= startX+1; x++)if (x>=0 && x<m_field_size_x){
				if (get_game_field(playernumber, y, x) != Stone.FIELD_DENIED){
					if (y != startY && x != startX){
						m_game_field[y][x] |= PLAYER_BIT_ALLOWED[playernumber];
					}else{
						m_game_field[y][x] &= ~PLAYER_BIT_ADDR[playernumber];
						m_game_field[y][x] |= PLAYER_BIT_DENIED[playernumber];
					}
				}
			}
		}
	}

	public int set_stone(Turn turn){
		int playernumber = turn.m_playernumber;
		Stone stone = m_player[playernumber].get_stone(turn.m_stone_number);
		stone.mirror_rotate_to(turn.m_mirror_count, turn.m_rotate_count);
		return set_stone(stone, playernumber, turn.m_y, turn.m_x);
	}

	public int set_stone(Stone stone, int playernumber, int startY, int startX) {
		for (int y = 0; y < stone.get_stone_size(); y++){
			for (int x = 0; x < stone.get_stone_size(); x++){
				if (stone.get_stone_field(y,x) != Stone.STONE_FIELD_FREE) {
					set_single_stone_for_player(playernumber, startY+y, startX+x); 
				}
			}
		}
		stone.available_decrement();
		refresh_player_data();
		return Stone.FIELD_ALLOWED;
	}

	protected void undo_turn(Turnpool turnpool){
		Turn turn = turnpool.m_tail;
		Stone stone = m_player[turn.m_playernumber].get_stone(turn.m_stone_number);
		int x, y;
		stone.mirror_rotate_to(turn.m_mirror_count, turn.m_rotate_count);

		//delete stone
		for (x = 0; x < stone.get_stone_size(); x++){
			for (y = 0; y < stone.get_stone_size(); y++){
				if (stone.get_stone_field(y, x) != Stone.STONE_FIELD_FREE){
					free_gamefield(turn.m_y + y, turn.m_x + x);
				}
			}
		}
		
		//redraw gamefield
		for (x = 0; x < m_field_size_x; x++){
			for (y = 0; y < m_field_size_y; y++){
				if (get_game_field(y, x) == Stone.FIELD_FREE ){
					free_gamefield(y, x);
				}
			}
		}
		for (x = 0; x < m_field_size_x; x++){
			for (y = 0; y < m_field_size_y; y++){
				if (get_game_field(y, x) != Stone.FIELD_FREE ){
					set_single_stone_for_player(get_game_field(y, x), y, x);
				}
			}
		}

		for (int p = 0; p < PLAYER_MAX; p++){
			if (get_game_field(p, get_player_start_y(p), get_player_start_x(p)) == Stone.FIELD_FREE){
				m_game_field[get_player_start_y(p)][get_player_start_x(p)] |= PLAYER_BIT_ALLOWED[p]; 
			}
		}
		stone.available_increment();
		refresh_player_data();
		//end redraw
		turnpool.delete_last();
	}
}
