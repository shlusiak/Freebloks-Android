package de.saschahlusiak.freebloks.game;

import de.saschahlusiak.freebloks.model.GameMode;
import de.saschahlusiak.freebloks.model.Board;

import java.io.Serializable;

public class GameConfiguration implements Serializable {
	public static int[] DEFAULT_STONE_SET = {
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1
	};

	public static int[] JUNIOR_STONE_SET = {
		2,
		2,
		2, 2,
		2, 2, 2, 2, 2,
		2, 0, 0, 0, 0, 0, 0, 0, 2, 2, 0, 0
	};

	public static final int DEFAULT_DIFFICULTY = 10;


	private String server;
	private boolean showLobby;
	private boolean requestPlayer[];
	private int stones[];
	private int difficulty;
	private GameMode gameMode;
	private int fieldSize;

	public String getServer() {
		return server;
	}

	public boolean getShowLobby() {
		return showLobby;
	}

	public boolean[] getRequestPlayers() {
		return requestPlayer;
	}

	public int[] getStones() {
		return stones;
	}

	public int getDifficulty() {
		return difficulty;
	}

	public GameMode getGameMode() {
		return gameMode;
	}

	public int getFieldSize() {
		return fieldSize;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private GameConfiguration configuration;

		public Builder() {
			configuration = new GameConfiguration();

			configuration.fieldSize = Board.DEFAULT_BOARD_SIZE;
			configuration.difficulty = DEFAULT_DIFFICULTY;
			configuration.gameMode = GameMode.GAMEMODE_4_COLORS_4_PLAYERS;
		}

		public GameConfiguration build() {
			if (configuration.getGameMode() == null)
				throw new IllegalArgumentException("game mode must not be null");

			return configuration;
		}

		public Builder server(String server) {
			configuration.server = server;
			return this;
		}

		public Builder showLobby(boolean showLobby) {
			configuration.showLobby = showLobby;
			return this;
		}

		public Builder requestPlayers(boolean[] requestPlayers) {
			configuration.requestPlayer = requestPlayers;
			return this;
		}

		public Builder stones(int[] stones) {
			configuration.stones = stones;
			return this;
		}

		public Builder fieldSize(int fieldSize) {
			configuration.fieldSize = fieldSize;
			return this;
		}

		public Builder difficulty(int difficulty) {
			configuration.difficulty = difficulty;
			return this;
		}

		public Builder gameMode(GameMode gameMode) {
			configuration.gameMode = gameMode;
			return this;
		}
	}
}
