package de.saschahlusiak.freebloks.model;

public class Ki {
	int num_threads;
	static final int BIGGEST_X_STONES = 7;
	Turnpool m_turnpool = new Turnpool();
	
	public static final int PERFECT = 0;
	public static final int HARD = 5;
	public static final int MEDIUM = 50;
	public static final int EASY = 120;
	
	public Ki(int num_threads) {
		this.num_threads = num_threads;
	}	
	
	private void calculate_possible_turns(Spiel spiel, Stone stone, int playernumber){
		for (int x = 0; x < spiel.m_field_size_x; x++){
			for (int y = 0; y < spiel.m_field_size_y; y++){
				if (spiel.get_game_field(playernumber, y, x) == Stone.FIELD_ALLOWED){
					calculate_possible_turns_in_position(spiel, stone, playernumber, y, x);
				}
			}	
		}	
	}


	private void calculate_possible_turns_in_position(Spiel spiel, Stone stone, int playernumber, int fieldY, int fieldX) {
		int mirror;
			
		int rotate_count = stone.m_rotate_counter;
		int mirror_count = stone.m_mirror_counter;

		if (stone.get_mirrorable() == Stone.MIRRORABLE_IMPORTANT) mirror = 1;
		else mirror = 0;

		for (int m = 0; m <= mirror; m++){
			for (int r = 0; r < stone.get_rotateable(); r++){
				stone.mirror_rotate_to(m, r);
				for (int x = 0; x < stone.get_stone_size(); x++){
					for (int y = 0; y < stone.get_stone_size(); y++){
						
						if (stone.get_stone_field(y, x) == Stone.STONE_FIELD_ALLOWED) {      					
							if (spiel.is_valid_turn(stone, playernumber, fieldY-y, fieldX-x) == Stone.FIELD_ALLOWED){
								m_turnpool.add_turn(playernumber, stone, fieldY-y, fieldX-x);
							}
						}
					}
				}
			}
		}
		stone.mirror_rotate_to(mirror_count, rotate_count);
	}

	class KiThread extends Thread {
		int from, to;
		int best_points;
		int current_player;
		int ki_fehler;
		Turn best;
		Spiel spiel;
		
		KiThread() {
			setPriority(Thread.NORM_PRIORITY - 2);
		}

		@Override
		public void run() {
			if (from > to)
				return;
			
			int new_points;
			Spiel follow = new Spiel(spiel);
			
			follow.follow_situation(current_player, spiel, m_turnpool.get_turn(from));
			best_points = get_ultimate_points(follow, current_player, ki_fehler, m_turnpool.get_turn(from));
			best = m_turnpool.get_turn(from);
			
			for (int n = from + 1; n <= to; n++) {
				follow.follow_situation(current_player, spiel, m_turnpool.get_turn(n));
				new_points = get_ultimate_points(follow, current_player, ki_fehler, m_turnpool.get_turn(n));
				
				if (new_points >= best_points) {
					best = m_turnpool.get_turn(n);
					best_points = new_points;
				}
			}
			
		}
	}

	private Turn get_ultimate_turn(Spiel spiel, int current_player, int ki_fehler) {
		build_up_turnpool_biggest_x_stones(spiel, current_player, BIGGEST_X_STONES);
		
		Turn best;
		int best_points;
		Spiel follow_situation = new Spiel(spiel);
		int i;
		KiThread threads[] = new KiThread[num_threads];

		for (i = 0; i < num_threads; i++)
		{
			threads[i] = new KiThread();
			threads[i].best=null;
			threads[i].best_points=0;
			threads[i].current_player=current_player;
			threads[i].ki_fehler=ki_fehler;
			threads[i].spiel=spiel;

			threads[i].from=2+i*(m_turnpool.get_number_of_stored_turns()-1)/num_threads;
			threads[i].to=2+(i+1)*(m_turnpool.get_number_of_stored_turns()-1)/num_threads-1;
			if (i==num_threads-1)threads[i].to=m_turnpool.get_number_of_stored_turns();

			threads[i].start();
		}

		best = m_turnpool.get_turn(1);
		follow_situation.follow_situation(current_player, spiel, best);

		best_points = get_ultimate_points(follow_situation, current_player, ki_fehler, best);
	 
		for (i = 0; i < num_threads; i++)
		{
			try {
				threads[i].join();
				if (threads[i].best_points > best_points && threads[i].best != null)
				{
					best_points = threads[i].best_points;
					best = threads[i].best;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				return null;
			}
		}

		return best;
	}


	private void build_up_turnpool_biggest_x_stones(Spiel spiel, int playernumber, int max_stored_stones) {
		m_turnpool.begin_add();
		for (int n = Stone.STONE_COUNT_ALL_SHAPES - 1; n >= 0; n--){
			Stone stone = spiel.get_player(playernumber).get_stone(n);
			if (stone.get_available() > 0){
				int old = m_turnpool.get_number_of_stored_turns();
				calculate_possible_turns(spiel, stone, playernumber);
				if (m_turnpool.get_number_of_stored_turns() > old) {
					if (--max_stored_stones <= 0)
						return;
				}
			}
		}
	}



	private static int get_distance_points(Spiel follow_situation, int playernumber, Turn turn){
		Stone stone = follow_situation.get_player(playernumber).get_stone(turn.m_stone_number);
		int summe = Math.abs(follow_situation.get_player_start_x(playernumber) - turn.m_x + stone.get_stone_size()/2);
		summe += Math.abs(follow_situation.get_player_start_y(playernumber) - turn.m_y+ stone.get_stone_size()/2);
		return summe;
	}


	private static int get_ultimate_points(Spiel follow_situation, int playernumber, int ki_fehler, Turn turn) {
		int summe = 0;
		for (int p = 0; p < Spiel.PLAYER_MAX; p++){
			if (p != playernumber) {
				if (p != follow_situation.get_player(playernumber).m_teammate){
					summe -= follow_situation.get_player(p).m_position_points;
				}
			}else{
				summe += follow_situation.get_player(p).m_position_points;
				summe -= follow_situation.get_player(p).m_stone_points_left * 175;
			}
		}
		summe += get_distance_points(follow_situation, playernumber, turn) * 20;
		return ((100 + (int)(Math.random() * (double)(ki_fehler+1))) * summe) / 100;
	}



	public Turn get_ki_turn(Spiel spiel, int playernumber, int ki_fehler){
		if (spiel.get_player(playernumber).m_number_of_possible_turns == 0) return null;
		return get_ultimate_turn(spiel, playernumber, ki_fehler);
	}
	
	public int get_number_of_stored_turns() {
		return m_turnpool.get_number_of_stored_turns();
	}
}
