package de.saschahlusiak.freebloks.model;

import java.io.Serializable;

import de.saschahlusiak.freebloks.controller.GameMode;
import de.saschahlusiak.freebloks.controller.GameStateException;

/**
 * State of a game (model).
 */
public class Spiel implements Serializable, Cloneable {
	private static final long serialVersionUID = -3803056324652460783L;

	/**
	 * Maximum number of players possible
	 */
	public static final int PLAYER_MAX = 4;

	/**
	 * Default board size
	 */
	public static final int DEFAULT_BOARD_SIZE = 20;

	/**
	 * A field is encoded in 8 bits, or 2 bits per player.
	 *
	 * No bit set: Stone.FIELD_FREE
	 * first bit set: Stone.FIELD_ALLOWED (shares a corner)
	 * second bit set: Stone.FIELD_DENIED (shares an edge)
	 *
	 * if first 6 bits are set, the lower most two bits encode the player number occupying the field
	 */
	private static final int PLAYER_BIT_ADDR[] = {
		0x01 | 0x02, // 00000011b
		0x04 | 0x08, // 00001100b
		0x10 | 0x20, // 00110000b
		0x40 | 0x80  // 11000000b
	};

	/**
	 * Bit mask to filter for allowed fields
	 */
	private static final int PLAYER_BIT_ALLOWED[] = {
		0x01, // 00000001b
		0x04, // 00000100b
		0x10, // 00010000b
		0x40  // 01000000b
	};

	/**
	 * Bit mask to filter for denied fields
	 */
	private static final int PLAYER_BIT_DENIED[] = {
		0x02, // 00000010b
		0x08, // 00001000b
		0x20, // 00100000b
		0x80  // 10000000b
	};

	/**
	 * If field value has first 6 bits set, the lower two bits encode the player number owning the field
	 */
	private static final int PLAYER_BIT_HAVE_MIN = 252;

	/**
	 * Current board size (x)
	 */
	public int width;

	/**
	 * Current board size (y)
	 */
	public int height;

	/**
	 * Encapsulated player information
	 */
	private Player player[] = new Player[PLAYER_MAX];

	/**
	 * One dimensional field [y * width + x]
	 */
	private int field[];

	/**
	 * New (initial) game state
	 */
	public Spiel(int size) {
		field = null;
		this.height = size;
		this.width = size;
		for (int i = 0; i < PLAYER_MAX; i++)
			player[i] = new Player();
		field = new int[this.width * this.height];
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		Spiel c = (Spiel)super.clone();
		c.player = player.clone();
		for (int i = 0; i < PLAYER_MAX; i++)
			c.player[i] = (Player) player[i].clone();
		c.field = field.clone();
		return c;
	}

	/**
	 * Mark field as Stone.FIELD_ALLOWED for given player, if field is free
	 */
	private void setSeed(int x, int y, int player) {
		if (getFieldStatus(player, y, x) == Stone.FIELD_FREE)
			field[y * width + x] = PLAYER_BIT_ALLOWED[player];
	}

	/**
	 * Set all seeds for given game mode. Called upon undo, so fields may still be occupied.
	 */
	private void setSeeds(GameMode gamemode) {
		if (gamemode == GameMode.GAMEMODE_DUO || gamemode == GameMode.GAMEMODE_JUNIOR) {
			setSeed(4, height - 5, 0);
			setSeed(width - 5, 4, 2);
		} else {
			for (int p = 0; p < PLAYER_MAX; p++){
				setSeed(getPlayerStartX(p), getPlayerStartY(p), p);
			}
		}
	}

	/**
	 * @return The entire board
	 */
	public final int[] getFields() {
		return field;
	}

	/**
	 * Returns whether for a given player the given field is allowed, denied or free
	 *
	 * @return One of Stone.FIELD_ALLOWED, Stone.FIELD_DENIED or Stone.FIELD_FREE.
	 */
	final public int getFieldStatus(int player, int y, int x) {
		int value = field[y * width + x];

		// if a field is occupied, it's denied
		if (value >= PLAYER_BIT_HAVE_MIN) return Stone.FIELD_DENIED;
		value &= PLAYER_BIT_ADDR[player];

		// otherwise it is either allowed, denied or free
		if (value == PLAYER_BIT_ALLOWED[player]) return Stone.FIELD_ALLOWED;
		if (value == PLAYER_BIT_DENIED[player]) return Stone.FIELD_DENIED;

		return Stone.FIELD_FREE;
	}

	/**
	 * @return The value of the player occupying the field or Stone.FIELD_FREE
	 */
	public final int getFieldPlayer(int y, int x) {
		int value = field[y * width + x];
		if (value < PLAYER_BIT_HAVE_MIN)
			return Stone.FIELD_FREE;
		return value & 3;
	}

	/**
	 * @return Player instance for given player
	 */
	public final Player getPlayer(int number) {
		return player[number];
	}

	/**
	 * @return The x-coordinate of the player's start corner
	 */
	public final int getPlayerStartX(int player) {
		switch (player) {
		case 0 :
		case 1 : return 0;
		default: return width - 1;
		}
	}

	/**
	 * @return The y-coordinate of the player's start corner
	 */
	public final int getPlayerStartY(int player) {
		switch (player){
		case 1 :
		case 2 : return 0;
		default: return height - 1;
		}
	}

	/**
	 * Sets number of available stones for each stone with specific size.
	 *
	 * @deprecated
	 */
	public void setAvailableStones(int one, int two, int three, int four, int five){
		int counts[] = {one, two, three, four, five};

		for (int n = 0 ; n < Stone.STONE_COUNT_ALL_SHAPES; n++) {
			for (int p = 0; p < PLAYER_MAX; p++) {
				final Stone stone = player[p].get_stone(n);
				stone.set_available(counts[stone.get_stone_points() - 1]);
			}
		}

		refreshPlayerData();
	}

	/**
	 * Sets the number of available stones for each stone.
	 */
	public void setAvailableStones(int stone_numbers[]) {
		for (int n = 0 ; n < Stone.STONE_COUNT_ALL_SHAPES; n++){
			for (int p = 0; p < PLAYER_MAX; p++){
				final Stone stone = player[p].get_stone(n);
				stone.set_available(stone_numbers[n]);
			}
		}

		refreshPlayerData();
	}

	/**
	 * Sets the members of team1 and team2 (for 2 players 4 colors mode)
	 */
	public void setTeams(int player_team1_1, int player_team1_2, int player_team2_1, int player_team2_2) {
		player[player_team1_1].set_teammate(player_team1_2);
		player[player_team1_2].set_teammate(player_team1_1);
		player[player_team1_1].set_nemesis(player_team2_1);
		player[player_team1_2].set_nemesis(player_team2_1);

		player[player_team2_1].set_teammate(player_team2_2);
		player[player_team2_2].set_teammate(player_team2_1);
		player[player_team2_1].set_nemesis(player_team1_1);
		player[player_team2_2].set_nemesis(player_team1_1);
	}

	/**
	 * Initialise board and player state
	 */
	public void startNewGame(GameMode gamemode)
	{
		startNewGame(gamemode, width, height);
	}

	/**
	 * Initialise board and player state
	 */
	public void startNewGame(GameMode gamemode, int width, int height) {
		this.width = width;
		this.height = height;

		field = new int[this.width * this.height];
		setSeeds(gamemode);
		for (int n = 0; n < PLAYER_MAX; n++) {
			player[n].init(this, n);
		}
	}

	/**
	 * Refresh player metadata for current board state, like number of possible turns, etc.
	 */
	public final void refreshPlayerData() {
		for (int n = 0; n < PLAYER_MAX; n++) {
			player[n].refresh_data(this);
		}
	}

	/**
	 * Check whether the given stone/player/position is a valid move.
	 *
	 * @return Stone.FIELD_ALLOWED if allowed, Stone.FIELD_DENIED otherwise
	 */
	public final int isValidTurn(Stone stone, int player, int startY, int startX, int mirror, int rotate) {
		int valid = Stone.FIELD_DENIED;
		int field_value;

		for (int y = 0; y < stone.get_stone_size(); y++){
			for (int x = 0; x < stone.get_stone_size(); x++){
				if (stone.get_stone_field(y, x, mirror, rotate) != Stone.STONE_FIELD_FREE) {
					if (y + startY < 0 || y + startY >= height || x + startX < 0 || x + startX >= width)
						return Stone.FIELD_DENIED;

					field_value = getFieldStatus(player, y + startY , x + startX);
					if (field_value == Stone.FIELD_DENIED) return Stone.FIELD_DENIED;
					if (field_value == Stone.FIELD_ALLOWED) valid = Stone.FIELD_ALLOWED;
				}
			}
		}
		return valid;
	}

	/**
	 * Check whether the given stone/player/position is a valid move.
	 */
	public final int isValidTurn(Turn turn) {
		final int player = turn.m_playernumber;
		final Stone stone = this.player[player].get_stone(turn.m_stone_number);

		return isValidTurn(stone, player, turn.m_y, turn.m_x, turn.m_mirror_count, turn.m_rotate_count);
	}

	/**
	 * Clear the given field (set to 0) [Stone.FIELD_FREE]
	 */
	private void clearField(int x, int y) {
		field[y * width + x] = 0;
	}

	/**
	 * Marks a single field as owned by a player.
	 *
	 * 1. mark that field as owned
	 * 2. mark edges as denied
	 * 3. mark corners as allowed (unless already denied)
	 */
	private void setSingleStone(int player, int fieldY, int fieldX) throws GameStateException {
		if (getFieldPlayer(fieldY, fieldX) != Stone.FIELD_FREE)
			throw new GameStateException("field already set");
		
		field[fieldY * width + fieldX] = PLAYER_BIT_HAVE_MIN | player;

		for (int y = fieldY - 1; y <= fieldY + 1; y++) if (y >= 0 && y < height) {
			for (int x = fieldX - 1; x <= fieldX + 1; x++) if (x >= 0 && x < width){
				if (getFieldStatus(player, y, x) != Stone.FIELD_DENIED) {
					final int idx = y * width + x;
					if (y != fieldY && x != fieldX) {
						// mark the corners as allowed (unless already denied)
						field[idx] |= PLAYER_BIT_ALLOWED[player];
					} else {
						// mark the edges as denied
						field[idx] &= ~PLAYER_BIT_ALLOWED[player];
						field[idx] |=  PLAYER_BIT_DENIED[player];
					}
				}
			}
		}
	}

	/**
	 * Clear the PLAYER_BIT_ALLOWED bit, because there is no possible valid turn for this field.
	 */
	final void clearAllowedBit(int player, int y, int x) {
		field[y * width + x] &= ~PLAYER_BIT_ALLOWED[player];
	}

	/**
	 * Execute a Turn and place the stone on the field
	 */
	public final void setStone(Turn turn) throws GameStateException{
		final Stone stone = player[turn.m_playernumber].get_stone(turn.m_stone_number);
		setStone(stone, turn.m_playernumber, turn.m_y, turn.m_x, turn.m_mirror_count, turn.m_rotate_count);
	}

	/**
	 * Places given stone onto field
	 */
	private void setStone(Stone stone, int player, int startY, int startX, int mirror, int rotate) throws GameStateException {
		for (int y = 0; y < stone.m_size; y++){
			for (int x = 0; x < stone.m_size; x++){
				if (stone.get_stone_field(y, x, mirror, rotate) != Stone.STONE_FIELD_FREE) {
					setSingleStone(player, startY+y, startX+x);
				}
			}
		}

		stone.available_decrement();

		this.player[player].m_lastStone = stone;
		refreshPlayerData();
	}

	/**
	 * Undo the last turn in the turnpool for the given gamemode
	 */
	public void undo(Turnpool turnpool, GameMode gamemode) throws GameStateException{
		final Turn turn = turnpool.pop();
		final Stone stone = player[turn.m_playernumber].get_stone(turn.m_stone_number);
		int x, y;

		// remove stone
		for (x = 0; x < stone.get_stone_size(); x++) {
			for (y = 0; y < stone.get_stone_size(); y++) {
				if (stone.get_stone_field(y, x, turn.m_mirror_count, turn.m_rotate_count) != Stone.STONE_FIELD_FREE) {
					if (getFieldPlayer(turn.m_y + y, turn.m_x + x) == Stone.FIELD_FREE)
						throw new GameStateException("field is free but shouldn't");
					clearField(turn.m_x + x, turn.m_y + y);
				}
			}
		}

		// clear all markers for the entire board
		for (x = 0; x < width; x++) {
			for (y = 0; y < height; y++) {
				if (getFieldPlayer(y, x) == Stone.FIELD_FREE ) {
					clearField(x, y);
				}
			}
		}
		
		// place all existing stones again to recreate the markers
		for (x = 0; x < width; x++) {
			for (y = 0; y < height; y++) {
				if (getFieldPlayer(y, x) != Stone.FIELD_FREE ) {
					int player = getFieldPlayer(y, x);
					clearField(x, y);
					setSingleStone(player, y, x);
				}
			}
		}

		// try to set all seeds again, in case we cleared up the starting points
		setSeeds(gamemode);

		stone.available_increment();
		refreshPlayerData();
	}
}
