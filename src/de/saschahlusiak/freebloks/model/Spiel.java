package de.saschahlusiak.freebloks.model;

import java.io.Serializable;

import de.saschahlusiak.freebloks.controller.GameMode;
import de.saschahlusiak.freebloks.controller.GameStateException;

public class Spiel implements Serializable, Cloneable {
	private static final long serialVersionUID = -3803056324652460783L;

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
			 0x01, //0
			 0x04, //1
			 0x10, //2
			 0x40  //3
		};

	static final int PLAYER_BIT_DENIED[] = {
			  2, //0
			  8, //1
			 32, //2
			128  //3
		};

	static final int PLAYER_BIT_HAVE_MIN = 252;

	public int m_field_size_y;
	public int m_field_size_x;

	Player m_player[] = new Player[PLAYER_MAX];
	private int m_game_field[];

	public Spiel(int size_y, int size_x) {
		m_game_field = null;
		m_field_size_y = size_y;
		m_field_size_x = size_x;
		for (int i = 0; i < PLAYER_MAX; i++)
			m_player[i] = new Player();
		init_field();
	}

	public Spiel(Spiel s) {
		m_field_size_y = s.m_field_size_y;
		m_field_size_x = s.m_field_size_x;

		m_game_field = new int[m_field_size_x * m_field_size_y];
		for (int i = 0; i < PLAYER_MAX; i++)
			m_player[i] = new Player();
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		Spiel c = (Spiel)super.clone();
		c.m_player = m_player.clone();
		for (int i = 0; i < PLAYER_MAX; i++)
			c.m_player[i] = (Player)m_player[i].clone();
		c.m_game_field = m_game_field.clone();
		return c;
	}

	private void init_field() {
		m_game_field = new int[m_field_size_x * m_field_size_y];
	}

	public void set_field_size(int width, int height) {
		m_field_size_x = width;
		m_field_size_y = height;
	}

	private void set_seed(int x, int y, int player) {
		if (get_game_field(player, y, x) == Stone.FIELD_FREE)
			m_game_field[y * m_field_size_x + x] = PLAYER_BIT_ALLOWED[player];
	}

	private void set_seeds(GameMode gamemode) {

		if (gamemode == GameMode.GAMEMODE_DUO || gamemode == GameMode.GAMEMODE_JUNIOR) {
			set_seed(4, m_field_size_y - 5, 0);
			set_seed(m_field_size_x - 5, 4, 2);
		} else {
			for (int p = 0; p < PLAYER_MAX; p++){
				set_seed(get_player_start_x(p), get_player_start_y(p), p);
			}
		}
	}

	public final int[] get_game_field() {
		return m_game_field;
	}

	void follow_situation(int vorher_playernumber, Spiel vorher_situation, Turn turn) throws GameStateException {
		int i;

		System.arraycopy(vorher_situation.m_game_field, 0, m_game_field, 0, m_game_field.length);

		for (i = 0; i < PLAYER_MAX; i++) {
			m_player[i].copyFrom(vorher_situation.m_player[i]);
		}

		set_stone(turn);
	}

	final public int get_game_field(int playernumber, int y, int x) {
		int wert = m_game_field[y * m_field_size_x + x];
		if (wert >= PLAYER_BIT_HAVE_MIN) return Stone.FIELD_DENIED;
		wert &= PLAYER_BIT_ADDR[playernumber];
		if (wert == 0) return Stone.FIELD_FREE;
		if (wert > PLAYER_BIT_ALLOWED[playernumber]) return Stone.FIELD_DENIED;
		return Stone.FIELD_ALLOWED;
	}

	public final int get_game_field(int y, int x) {
		int wert = m_game_field[y * m_field_size_x + x];
		if (wert < PLAYER_BIT_HAVE_MIN)
			return Stone.FIELD_FREE;
		return wert & 3;
	}

	public final Player get_player(int playernumber) {
		return m_player[playernumber];
	}

	public final int get_player_start_x(int playernumber) {
		switch (playernumber) {
		case 0 :
		case 1 : return 0;
		default: return m_field_size_x - 1;
		}
	}

	public final int get_player_start_y(int playernumber) {
		switch (playernumber){
		case 1 :
		case 2 : return 0;
		default: return m_field_size_y - 1;
		}
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
	
	public void set_stone_numbers(int stone_numbers[]) {
		for (int n = 0 ; n < Stone.STONE_COUNT_ALL_SHAPES; n++){
			for (int p = 0; p < PLAYER_MAX; p++){
				Stone stone = m_player[p].get_stone(n);
				stone.set_available(stone_numbers[n]);
			}
		}

		refresh_player_data();
	}


	public void set_teams(int player_team1_1, int player_team1_2, int player_team2_1, int player_team2_2){
		m_player[player_team1_1].set_teammate(player_team1_2);
		m_player[player_team1_2].set_teammate(player_team1_1);
		m_player[player_team1_1].set_nemesis(player_team2_1);
		m_player[player_team1_2].set_nemesis(player_team2_1);

		m_player[player_team2_1].set_teammate(player_team2_2);
		m_player[player_team2_2].set_teammate(player_team2_1);
		m_player[player_team2_1].set_nemesis(player_team1_1);
		m_player[player_team2_2].set_nemesis(player_team1_1);
	}

	public void start_new_game(GameMode gamemode){
		init_field();
		set_seeds(gamemode);
		for (int n = 0; n < PLAYER_MAX; n++){
			m_player[n].init(this, n);
		}
	}

	public final void refresh_player_data(){
		for (int n = 0; n < PLAYER_MAX; n++) {
			m_player[n].refresh_data(this);
		}
	}

	public final int is_valid_turn(Stone stone, int playernumber, int startY, int startX) {
		int valid = Stone.FIELD_DENIED;
		int field_value;

		for (int y = 0; y < stone.get_stone_size(); y++){
			for (int x = 0; x < stone.get_stone_size(); x++){
				if (stone.get_stone_field(y,x) != Stone.STONE_FIELD_FREE) {
					if (y + startY < 0 || y + startY >= m_field_size_y || x + startX < 0 || x + startX >= m_field_size_x)
						return Stone.FIELD_DENIED;

					field_value = get_game_field(playernumber, y + startY , x + startX);
					if (field_value == Stone.FIELD_DENIED) return Stone.FIELD_DENIED;
					if (field_value == Stone.FIELD_ALLOWED) valid = Stone.FIELD_ALLOWED;
				}
			}
		}
		return valid;
	}

	public final int is_valid_turn(Turn turn) {
		int playernumber = turn.m_playernumber;
		Stone stone = m_player[playernumber].get_stone(turn.m_stone_number);
		stone.mirror_rotate_to(turn.m_mirror_count, turn.m_rotate_count);
		return is_valid_turn(stone, playernumber, turn.m_y, turn.m_x);
	}

	final void free_gamefield(int y, int x) {
		m_game_field[y * m_field_size_x + x] = 0;
	}

	final void set_single_stone_for_player(int playernumber, int startY, int startX) throws GameStateException {
		if (get_game_field(startY, startX) != Stone.FIELD_FREE)
			throw new GameStateException("field already set");
		
		m_game_field[startY * m_field_size_x + startX] = PLAYER_BIT_HAVE_MIN | playernumber;
		for (int y = startY-1; y <= startY+1; y++) if (y>=0 && y<m_field_size_y) {
			for (int x = startX-1; x <= startX+1; x++) if (x>=0 && x<m_field_size_x){
				if (get_game_field(playernumber, y, x) != Stone.FIELD_DENIED) {
					int idx = y * m_field_size_x + x;
					if (y != startY && x != startX) {
						m_game_field[idx] |= PLAYER_BIT_ALLOWED[playernumber];
					} else {
						m_game_field[idx] &= ~PLAYER_BIT_ADDR[playernumber];
						m_game_field[idx] |=  PLAYER_BIT_DENIED[playernumber];
					}
				}
			}
		}
	}

	final void set_field_free_bit(int playernumber, int y, int x) {
		m_game_field[y * m_field_size_x + x] &= ~PLAYER_BIT_ALLOWED[playernumber];
	}

	public final int set_stone(Turn turn) throws GameStateException{
		Stone stone = m_player[turn.m_playernumber].get_stone(turn.m_stone_number);
		stone.mirror_rotate_to(turn.m_mirror_count, turn.m_rotate_count);
		return set_stone(stone, turn.m_playernumber, turn.m_y, turn.m_x);
	}

	public final int set_stone(Stone stone, int playernumber, int startY, int startX) throws GameStateException {
		for (int y = 0; y < stone.m_size; y++){
			for (int x = 0; x < stone.m_size; x++){
				if (stone.get_stone_field(y,x) != Stone.STONE_FIELD_FREE) {
					set_single_stone_for_player(playernumber, startY+y, startX+x);
				}
			}
		}
		stone.available_decrement();
		m_player[playernumber].m_lastStone = stone;
		refresh_player_data();
		return Stone.FIELD_ALLOWED;
	}

	public void undo_turn(Turnpool turnpool, GameMode gamemode) throws GameStateException{
		Turn turn = turnpool.get_last_turn();
		Stone stone = m_player[turn.m_playernumber].get_stone(turn.m_stone_number);
		int x, y;
		stone.mirror_rotate_to(turn.m_mirror_count, turn.m_rotate_count);

		// remove stone
		for (x = 0; x < stone.get_stone_size(); x++) {
			for (y = 0; y < stone.get_stone_size(); y++) {
				if (stone.get_stone_field(y, x) != Stone.STONE_FIELD_FREE) {
					if (get_game_field(turn.m_y + y, turn.m_x + x) == Stone.FIELD_FREE)
						throw new GameStateException("field is free but shouldn't");
					free_gamefield(turn.m_y + y, turn.m_x + x);
				}
			}
		}

		// clear all markers
		for (x = 0; x < m_field_size_x; x++) {
			for (y = 0; y < m_field_size_y; y++) {
				if (get_game_field(y, x) == Stone.FIELD_FREE ) {
					free_gamefield(y, x);
				}
			}
		}
		
		// reset all layed stones to recreate markers
		for (x = 0; x < m_field_size_x; x++) {
			for (y = 0; y < m_field_size_y; y++) {
				if (get_game_field(y, x) != Stone.FIELD_FREE ) {
					int player = get_game_field(y, x);
					free_gamefield(y, x);
					set_single_stone_for_player(player, y, x);
				}
			}
		}

		set_seeds(gamemode);
		stone.available_increment();
		refresh_player_data();
		//end redraw
		turnpool.delete_last();
	}
}
