package de.saschahlusiak.freebloks.model;

import java.io.Serializable;
import java.util.Arrays;

import de.saschahlusiak.freebloks.controller.GameMode;

import static de.saschahlusiak.freebloks.controller.GameMode.*;

/**
 * Enriches the current board state with meta information about the game:
 *
 * - game state
 * - history for undo
 * - current player
 * - whether players are controlled locally or by the computer
 * - current game mode
 */
public class Spielleiter extends Spiel implements Serializable {
	private static final long serialVersionUID = -7880809258246268794L;

	public static final int PLAYER_COMPUTER = -2;
	public static final int PLAYER_LOCAL = -1;

	public int m_current_player;
	public int spieler[] = new int[Spiel.PLAYER_MAX];
	private GameMode m_gamemode;
	private boolean finished = false;
	private boolean started = false;

	public Turnpool history;

	public Spielleiter(int size) {
		super(size);
		m_current_player=-1;
		m_gamemode = GAMEMODE_4_COLORS_4_PLAYERS;
		for (int i=0;i<PLAYER_MAX;i++)spieler[i]=PLAYER_COMPUTER;
		history=new Turnpool();
	}

	public void set_noplayer() {
		m_current_player=-1;
	}

	public int current_player() {
		return m_current_player;
	}

	public Player get_current_player() {
		if (m_current_player == -1)
			return null;
		else
			return getPlayer(m_current_player);
	}

	public GameMode getGameMode() {
		return m_gamemode;
	}

	public void setGameMode(GameMode gameMode)
	{
		this.m_gamemode = gameMode;
	}

	public void addHistory(Turn turn) {
		history.add(new Turn(turn));
	}

	int num_players() {
		int n;
		n=0;
		for (int i=0;i<PLAYER_MAX;i++)if (spieler[i]!=PLAYER_COMPUTER)n++;
		return n;
	}

	/**
	 * Gibt true zurueck, wenn der Spieler kein Computerspieler ist
	 **/
	public boolean is_local_player(int player) {
		/*
		 * Bei keinem aktuellem Spieler, ist der aktuelle natuerlich nicht
		 * lokal.
		 */
		if (player == -1)
			return false;
		return (spieler[player] != Spielleiter.PLAYER_COMPUTER);
	}

	public boolean is_local_player() {
		return is_local_player(m_current_player);
	}

	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public boolean isStarted() {
		return started;
	}

	public void setStarted(boolean started) {
		this.started = started;
	}

	public PlayerData[] getResultData() {
		PlayerData[] data;
		int i;
		switch (m_gamemode) {
		case GAMEMODE_2_COLORS_2_PLAYERS:
		case GAMEMODE_DUO:
		case GAMEMODE_JUNIOR:
			data = new PlayerData[2];
			data[0] = new PlayerData(this, 0);
			data[1] = new PlayerData(this, 2);
			break;

		case GAMEMODE_4_COLORS_2_PLAYERS:
			data = new PlayerData[2];
			data[0] = new PlayerData(this, 0, 2);
			data[1] = new PlayerData(this, 1, 3);
			break;

		case GAMEMODE_4_COLORS_4_PLAYERS:
		default:
			data = new PlayerData[4];
			data[0] = new PlayerData(this, 0);
			data[1] = new PlayerData(this, 1);
			data[2] = new PlayerData(this, 2);
			data[3] = new PlayerData(this, 3);
			break;
		}

		Arrays.sort(data);
		int place;
		for (i = 0; i < data.length; i++) {
			place = i + 1;
			if (i > 0) {
				if (data[i].compareTo(data[i-1]) == 0)
					place = data[i-1].getPlace();
			}

			data[i].setPlace(place);
		}
		return data;
	}
}
