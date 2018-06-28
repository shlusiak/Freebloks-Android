package de.saschahlusiak.freebloks.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import com.crashlytics.android.Crashlytics;
import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.game.GameConfiguration;
import de.saschahlusiak.freebloks.model.Stone;
import de.saschahlusiak.freebloks.model.Turn;
import de.saschahlusiak.freebloks.network.*;

public class SpielClient {
	static final String tag = SpielClient.class.getSimpleName();
	static final int DEFAULT_TIMEOUT = 10000;

	private final ArrayList<SpielClientInterface> spielClientInterface = new ArrayList<>();
	private Socket client_socket;
	public Spielleiter spiel;
	private NET_SERVER_STATUS lastStatus;
	private HandlerThread sendThread;
	private Handler sendHandler;
	private GameConfiguration config;

	public SpielClient(Spielleiter leiter, GameConfiguration config) {
		this.config = config;
		if (leiter == null) {
			spiel = new Spielleiter(config.getFieldSize());
			spiel.startNewGame(GameMode.GAMEMODE_4_COLORS_4_PLAYERS);
			spiel.setAvailableStones(0, 0, 0, 0, 0);
		} else
			this.spiel = leiter;
		client_socket = null;
	}

	@Override
	protected void finalize() throws Throwable {
		disconnect();
		super.finalize();
	}

	public GameConfiguration getConfig() {
		return this.config;
	}

	public boolean isConnected() {
		return (client_socket != null) && (!client_socket.isClosed());
	}

	public synchronized void addClientInterface(SpielClientInterface sci) {
		this.spielClientInterface.add(sci);
	}

	public synchronized void removeClientInterface(SpielClientInterface sci) {
		this.spielClientInterface.remove(sci);
	}

	public void connect(Context context, String host, int port) throws IOException {
		try {
			SocketAddress address;
			if (host == null)
				address = new InetSocketAddress((InetAddress)null, port);
			else
				address = new InetSocketAddress(host, port);
			
			client_socket = new Socket();
			client_socket.connect(address, DEFAULT_TIMEOUT);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException(context.getString(R.string.connection_refused));
		}
		synchronized(this) {
			sendThread = new HandlerThread("SendThread");
			sendThread.start();
			sendHandler = new SendHandler(client_socket, sendThread.getLooper());

			for (SpielClientInterface sci : spielClientInterface)
				sci.onConnected(spiel);
		}
	}

	public synchronized void disconnect() {
		if (client_socket != null) {
			try {
				Crashlytics.log("Disconnecting from " + config.getServer());
				if(client_socket.isConnected())
					client_socket.shutdownInput();
				client_socket.close();
				if (sendThread != null)
					sendThread.quit();
				sendThread = null;
			} catch (IOException e) {
				e.printStackTrace();
			}

			for (SpielClientInterface sci : spielClientInterface)
				sci.onDisconnected(spiel);
		}
		client_socket = null;
	}

	public void poll() throws IOException {
		NET_HEADER pkg;

		try {
			final InputStream is;

			synchronized (this) {
				if (client_socket == null || client_socket.isInputShutdown())
					return;
				is = client_socket.getInputStream();
			}

			/* Read a complete network message into buffer */
			pkg = Network.read_package(is, true);
				
			if (pkg != null)
				process_message(pkg);
		}
		catch (GameStateException | ProtocolException e) {
			throw new RuntimeException(e);
		}
	}

	public void request_player(int player, String name) {
		send(new NET_REQUEST_PLAYER(player, name));
	}

	public void revoke_player(int player) {
		send(new NET_REVOKE_PLAYER(player));
	}
	
	public void request_game_mode(int width, int height, GameMode g, int stones[]) {
		send(new NET_REQUEST_GAME_MODE(width, height, g, stones));
	}

	public void request_hint(int player) {
		if (spiel == null)
			return;
		if (!isConnected())
			return;
		if (!spiel.is_local_player())
			return;
		send(new NET_REQUEST_HINT(player));
	}

	synchronized void process_message(NET_HEADER data) throws ProtocolException, GameStateException {
		int i;

		switch(data.msg_type)
		{
		/* Der Server gewaehrt dem Client einen lokalen Spieler */
		case Network.MSG_GRANT_PLAYER:
			if (spiel.isStarted())
				throw new GameStateException("received MSG_REVOKE_PLAYER but game is running");

			i=((NET_GRANT_PLAYER)data).player;

			/* Merken, dass es sich bei i um einen lokalen Spieler handelt */
			spiel.spieler[i] = Spielleiter.PLAYER_LOCAL;
			break;

		case Network.MSG_REVOKE_PLAYER:
			if (spiel.isStarted())
				throw new GameStateException("received MSG_REVOKE_PLAYER but game is running");

			i=((NET_REVOKE_PLAYER)data).player;
			if (spiel.spieler[i] != Spielleiter.PLAYER_LOCAL)
				throw new GameStateException("revoked player " + i + " is not local");
			spiel.spieler[i] = Spielleiter.PLAYER_COMPUTER;
			break;

		/* server sent new current player number */
		case Network.MSG_CURRENT_PLAYER:
			spiel.m_current_player=((NET_CURRENT_PLAYER)data).player;
			Crashlytics.log("New player: " + spiel.m_current_player);
			for (SpielClientInterface sci : spielClientInterface)
				sci.newCurrentPlayer(spiel.m_current_player);
			break;

		/* Nachricht des Servers ueber ein endgueltiges Setzen eines Steins auf das Feld */
		case Network.MSG_SET_STONE: {
			if (!spiel.isStarted() && !spiel.isFinished())
				throw new GameStateException("received MSG_SET_STONE but game not running");

			NET_SET_STONE s=(NET_SET_STONE)data;

			Turn turn = s.toTurn();

			Crashlytics.log(String.format("player %d, stone %d (x%d), x %d, y %d",
				s.player,
				s.stone,
				spiel.getPlayer(s.player).get_stone(s.stone).get_available(),
				s.x,
				s.y));

			spiel.addHistory(turn);

			/* inform listeners first, so that effects can be added before the stone
			 * is committed. fixes drawing glitches, where stone is set, but
			 * effect hasn't been added yet.
			 */
			for (SpielClientInterface sci : spielClientInterface)
				sci.stoneWillBeSet(s);

			if (spiel.isValidTurn(turn) == Stone.FIELD_DENIED)
			{
				throw new GameStateException("game not in sync");
			}

			spiel.setStone(turn);

			for (SpielClientInterface sci : spielClientInterface)
				sci.stoneHasBeenSet(s);

			break;
		}

		case Network.MSG_STONE_HINT: {
			for (SpielClientInterface sci : spielClientInterface)
				sci.hintReceived((NET_SET_STONE)data);
			break;
		}

		/* Server hat entschlossen, dass das Spiel vorbei ist */
		case Network.MSG_GAME_FINISH: {
			Crashlytics.log("Game finished");
			spiel.setFinished(true);
			for (SpielClientInterface sci : spielClientInterface)
				sci.gameFinished();
			break;
		}

		/* Ein Server-Status Paket ist eingetroffen, Inhalt merken */
		case Network.MSG_SERVER_STATUS:
		{
			NET_SERVER_STATUS status = (NET_SERVER_STATUS)data;
			/* Wenn Spielfeldgroesse sich von Server unterscheidet,
			   lokale Spielfeldgroesse hier anpassen */
			if (!spiel.isStarted()) {
				spiel.startNewGame(status.gamemode, status.width, status.height);
				if (status.isVersion(3))
					spiel.setAvailableStones(status.stone_numbers);
			}
			if (!status.isVersion(3)) {
				boolean changed = false;
				if (lastStatus == null)
					changed = true;
				else {
					for (i = 0; i < Stone.STONE_SIZE_MAX; i++)
						changed |= (lastStatus.stone_numbers_obsolete[i] != status.stone_numbers_obsolete[i]);
				}
				if (changed && !spiel.isStarted()) spiel.setAvailableStones(status.stone_numbers_obsolete[0],status.stone_numbers_obsolete[1],status.stone_numbers_obsolete[2],status.stone_numbers_obsolete[3],status.stone_numbers_obsolete[4]);
			}
			spiel.setGameMode(status.gamemode);
			if (spiel.getGameMode() == GameMode.GAMEMODE_4_COLORS_2_PLAYERS)
				spiel.setTeams(0, 2, 1, 3);
			if (spiel.getGameMode() == GameMode.GAMEMODE_2_COLORS_2_PLAYERS || spiel.getGameMode() == GameMode.GAMEMODE_DUO || spiel.getGameMode()==GameMode.GAMEMODE_JUNIOR)
			{
				for (int n = 0 ; n < Stone.STONE_COUNT_ALL_SHAPES; n++){
					spiel.getPlayer(1).get_stone(n).set_available(0);
					spiel.getPlayer(3).get_stone(n).set_available(0);
				}
			}
			lastStatus = status;
			for (SpielClientInterface sci : spielClientInterface)
				sci.serverStatus(status);
		}
		break;

		/* Server hat eine Chat-Nachricht geschickt. */
		case Network.MSG_CHAT: {
			for (SpielClientInterface sci : spielClientInterface)
				sci.chatReceived((NET_CHAT) data);
			break;
		}
		
		/* Der Server hat eine neue Runde gestartet. Spiel zuruecksetzen */
		case Network.MSG_START_GAME: {
			Crashlytics.log("Start game");

			spiel.startNewGame(spiel.getGameMode());
			spiel.setFinished(false);
			spiel.setStarted(true);
			/* Unbedingt history leeren. */
			if (spiel.history != null)
				spiel.history.clear();

//			setAvailableStones(status.stone_numbers[0],status.stone_numbers[1],status.stone_numbers[2],status.stone_numbers[3],status.stone_numbers[4]);
			if (spiel.getGameMode() == GameMode.GAMEMODE_4_COLORS_2_PLAYERS)
				spiel.setTeams(0, 2, 1, 3);
			if (spiel.getGameMode() == GameMode.GAMEMODE_2_COLORS_2_PLAYERS ||
				spiel.getGameMode() == GameMode.GAMEMODE_DUO ||
				spiel.getGameMode()== GameMode.GAMEMODE_JUNIOR)
			{
				for (int n = 0 ; n < Stone.STONE_COUNT_ALL_SHAPES; n++){
					spiel.getPlayer(1).get_stone(n).set_available(0);
					spiel.getPlayer(3).get_stone(n).set_available(0);
				}
			}
			spiel.m_current_player=-1;
			spiel.refreshPlayerData();
			for (SpielClientInterface sci : spielClientInterface)
				sci.gameStarted();
			break;
		}

		/* Server laesst den letzten Zug rueckgaengig machen */
		case Network.MSG_UNDO_STONE: {
			if (!spiel.isStarted() && !spiel.isFinished())
				throw new GameStateException("received MSG_UNDO_STONE but game not running");

			Turn t = spiel.history.getLast();

			Crashlytics.log(String.format("undo: player %d, stone %d, x %d, y %d",
				t.m_playernumber,
				t.m_stone_number,
				t.m_x,
				t.m_y));

			Log.d(tag, "stone undone: " + t.m_stone_number);
			for (SpielClientInterface sci : spielClientInterface)
				sci.stoneUndone( t);
			spiel.undo(spiel.history, spiel.getGameMode());
			break;
		}
		
		default: 
			throw new ProtocolException("don't know how to handle message " + data.msg_type);
		}
	}

	/**
	 * Wird von der GUI aufgerufen, wenn ein Spieler einen Stein setzen will Die
	 * Aktion wird nur an den Server geschickt, der Stein wird NICHT lokal
	 * gesetzt
	 **/
	public int set_stone(Turn turn)
	{
		NET_SET_STONE data;
		if (spiel.m_current_player==-1)
			return Stone.FIELD_DENIED;
		
		/* Lokal keinen Spieler als aktiv setzen.
	   	Der Server schickt uns nachher den neuen aktiven Spieler zu */
		spiel.set_noplayer();

		data = new NET_SET_STONE();
		/* Datenstruktur mit Daten der Aktion fuellen */
		data.player=turn.m_playernumber;
		data.stone=turn.m_stone_number;
		data.mirror_count=turn.m_mirror_count;
		data.rotate_count=turn.m_rotate_count;
		data.x=turn.m_x;
		data.y=turn.m_y;

		/* Nachricht ueber den Stein an den Server schicken */
		send(data);

		return Stone.FIELD_ALLOWED;
	}

	/**
	 * Erbittet den Spielstart beim Server
	 **/
	public void request_start() {
		send(new NET_START_GAME());
	}

	/**
	 * Erbittet eine Zugzuruecknahme beim Server
	 **/
	public void request_undo() {
		if (spiel == null)
			return;
		if (!isConnected())
			return;
		if (!spiel.is_local_player())
			return;
		send(new NET_REQUEST_UNDO());
	}


	synchronized public void send(NET_HEADER msg) {
		if (sendHandler == null)
			return;

		Message m = sendHandler.obtainMessage(1, msg);
		m.sendToTarget();
	}
}
