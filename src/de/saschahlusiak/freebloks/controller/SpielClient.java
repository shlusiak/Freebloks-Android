package de.saschahlusiak.freebloks.controller;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import android.content.Context;
import android.util.Log;

import de.saschahlusiak.freebloksvip.R;
import de.saschahlusiak.freebloks.model.Spiel;
import de.saschahlusiak.freebloks.model.Stone;
import de.saschahlusiak.freebloks.model.Turn;
import de.saschahlusiak.freebloks.network.*;

public class SpielClient {
	static final String tag = SpielClient.class.getSimpleName();
	
	ArrayList<SpielClientInterface> spielClientInterface = new ArrayList<SpielClientInterface>();
	Socket client_socket;
	String lastHost;
	public Spielleiter spiel;
	int lastDifficulty;
	boolean lastPlayers[];

	
	public SpielClient(Spielleiter leiter, int difficulty, boolean[] lastPlayers, int fieldsize) {
		this.lastDifficulty = difficulty;
		this.lastPlayers = lastPlayers;
		if (leiter == null) {
			spiel = new Spielleiter(fieldsize, fieldsize);
			spiel.start_new_game(Spielleiter.GAMEMODE_4_COLORS_4_PLAYERS);			
			spiel.set_stone_numbers(0, 0, 0, 0, 0);
		} else
			this.spiel = leiter;
		client_socket = null;
	}
	
	@Override
	protected void finalize() throws Throwable {
		disconnect();
		super.finalize();
	}
	
	public boolean[] getLastPlayers() {
		return lastPlayers;
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
	
	public synchronized void clearClientInterface() {
		spielClientInterface.clear();
	}



	public void connect(Context context, String host, int port) throws Exception {
		this.lastHost = host;
		try {
			client_socket = new Socket(host, port);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(context.getString(R.string.connection_refused));
		}
		for (SpielClientInterface sci : spielClientInterface)
			sci.onConnected(spiel);		
	}

	void setSocket(Socket client_socket) {
		this.client_socket = client_socket;
	}

	public synchronized void disconnect() {
		if (client_socket != null) {
			try {
				client_socket.shutdownInput();
				client_socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			for (SpielClientInterface sci : spielClientInterface)
				sci.onDisconnected(spiel);		
		}
		client_socket = null;
	}

	public Exception poll() {
		NET_HEADER pkg;
		/* Puffer fuer eine Netzwerknachricht. */
		
		/* Lese eine Nachricht komplett aus dem Socket in buffer */
		try {
			pkg = Network.read_package(client_socket, true);
		}
		catch (Exception e) {
			e.printStackTrace();
			return e;
		}
		
		/* TODO: implement some game specific exceptions, like GameNotInSyncException */
		if (pkg != null)
			process_message(pkg);
		
		return null;
	}

	public void request_player(int player, String name) {
		new NET_REQUEST_PLAYER(player, name).send(client_socket);
	}
	
	public void request_hint(int player) {
		if (spiel == null)
			return;
		if (!isConnected())
			return;
		if (!spiel.is_local_player())
			return;
		new NET_REQUEST_HINT(player).send(client_socket);
	}

	synchronized void process_message(NET_HEADER data) {
		int i;
		switch(data.msg_type)
		{
		/* Der Server gewaehrt dem Client einen lokalen Spieler */
		case Network.MSG_GRANT_PLAYER:
			i=((NET_GRANT_PLAYER)data).player;
			/* Merken, dass es sich bei i um einen lokalen Spieler handelt */
			spiel.spieler[i] = Spielleiter.PLAYER_LOCAL;
			break;

		/* Der Server hat einen aktuellen Spieler festgelegt */
		case Network.MSG_CURRENT_PLAYER: 
			spiel.m_current_player=((NET_CURRENT_PLAYER)data).player;
			for (SpielClientInterface sci : spielClientInterface)
				sci.newCurrentPlayer(spiel.m_current_player);
			break;
				
		/* Nachricht des Servers ueber ein endgueltiges Setzen eines Steins auf das Feld */
		case Network.MSG_SET_STONE: {
			NET_SET_STONE s=(NET_SET_STONE)data;
			/* Entsprechenden Stein des Spielers holen */
			Stone stone = spiel.get_player(s.player).get_stone(s.stone);
			/* Stein in richtige Position drehen */
			stone.mirror_rotate_to(s.mirror_count, s.rotate_count);
			/* Stein aufs echte Spielfeld setzen */
//			Log.d(tag, "player " + s.player + " stone " + stone.get_number() + " to (" + s.x + "," + s.y + ")");
			/* Zug der History anhaengen */
			spiel.addHistory(s.player, stone, s.y, s.x);
			/* inform listeners first, so that effects can be added before the stone
			 * is committed. fixes drawing glitches, where stone is set, but
			 * effect hasn't been added yet
			 */
			for (SpielClientInterface sci : spielClientInterface)
				sci.stoneWillBeSet(s);
			
			if ((spiel.is_valid_turn(stone, s.player, s.y, s.x) == Stone.FIELD_DENIED) ||
			   (spiel.set_stone(stone, s.player, s.y, s.x) != Stone.FIELD_ALLOWED))
			{	// Spiel scheint nicht mehr synchron zu sein
				// GAANZ schlecht!!
				throw new IllegalStateException("game not in sync");
			}

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
			if (status.width != spiel.m_field_size_x || status.height != spiel.m_field_size_y) {
				spiel.set_field_size(status.width, status.height);
				spiel.start_new_game(status.gamemode);
			}
			{
				boolean changed=false;
				for (i = 0; i < Stone.STONE_SIZE_MAX; i++)
				{
					changed |= (status.stone_numbers[i] != status.stone_numbers[i]);
					status.stone_numbers[i]=status.stone_numbers[i];
				}
				if (changed)spiel.set_stone_numbers(status.stone_numbers[0],status.stone_numbers[1],status.stone_numbers[2],status.stone_numbers[3],status.stone_numbers[4]);
			}
			spiel.m_gamemode = status.gamemode;
			if (spiel.m_gamemode == Spielleiter.GAMEMODE_4_COLORS_2_PLAYERS)
				spiel.set_teams(0, 2, 1, 3);
			if (spiel.m_gamemode==Spielleiter.GAMEMODE_2_COLORS_2_PLAYERS
				|| spiel.m_gamemode==Spielleiter.GAMEMODE_DUO)
			{
				for (int n = 0 ; n < Stone.STONE_COUNT_ALL_SHAPES; n++){
					spiel.get_player(1).get_stone(n).set_available(0);
					spiel.get_player(3).get_stone(n).set_available(0);
				}
			}
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
			spiel.start_new_game(spiel.m_gamemode);
			spiel.setFinished(false);
			/* Unbedingt history leeren. */
			if (spiel.history != null)
				spiel.history.delete_all_turns();

//			set_stone_numbers(status.stone_numbers[0],status.stone_numbers[1],status.stone_numbers[2],status.stone_numbers[3],status.stone_numbers[4]);
			if (spiel.m_gamemode==Spielleiter.GAMEMODE_4_COLORS_2_PLAYERS)
				spiel.set_teams(0,2,1,3);
			if (spiel.m_gamemode==Spielleiter.GAMEMODE_2_COLORS_2_PLAYERS ||
				spiel.m_gamemode==Spielleiter.GAMEMODE_DUO)
			{
				for (int n = 0 ; n < Stone.STONE_COUNT_ALL_SHAPES; n++){
					spiel.get_player(1).get_stone(n).set_available(0);
					spiel.get_player(3).get_stone(n).set_available(0);
				}
			}
			spiel.m_current_player=-1;
			spiel.refresh_player_data();
			for (SpielClientInterface sci : spielClientInterface)
				sci.gameStarted();
			break;
		}
	
		/* Server laesst den letzten Zug rueckgaengig machen */
		case Network.MSG_UNDO_STONE: {
			Turn t = spiel.history.get_last_turn();
			Stone stone = spiel.get_player(t.m_playernumber).get_stone(t.m_stone_number);
			stone.mirror_rotate_to(t.m_mirror_count, t.m_rotate_count);
			Log.d(tag, "stone undone: " + t.m_stone_number);
			for (SpielClientInterface sci : spielClientInterface)
				sci.stoneUndone(stone, t);
			spiel.undo_turn(spiel.history, spiel.m_gamemode);
			break;
		}
		default: Log.e(tag, "FEHLER: unbekannte Nachricht empfangen: #" + data.msg_type);
			break;
		}
	}

	/**
	 * Wird von der GUI aufgerufen, wenn ein Spieler einen Stein setzen will Die
	 * Aktion wird nur an den Server geschickt, der Stein wird NICHT lokal
	 * gesetzt
	 **/
	public int set_stone(Stone stone, int y, int x)
	{
		NET_SET_STONE data;
		if (spiel.m_current_player==-1)return Stone.FIELD_DENIED;

		data = new NET_SET_STONE();
		/* Datenstruktur mit Daten der Aktion fuellen */
		data.player=spiel.m_current_player;
		data.stone=stone.get_number();
		data.mirror_count=stone.get_mirror_counter();
		data.rotate_count=stone.get_rotate_counter();
		data.x=x;
		data.y=y;

		
		/* Nachricht ueber den Stein an den Server schicken */
		data.send(client_socket);

		/* Lokal keinen Spieler als aktiv setzen.
	   	Der Server schickt uns nachher den neuen aktiven Spieler zu */
		spiel.set_noplayer();
		return Stone.FIELD_ALLOWED;
	}

	/**
	 * Erbittet den Spielstart beim Server
	 **/
	public void request_start() {
		new NET_START_GAME().send(client_socket);
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
		new NET_REQUEST_UNDO().send(client_socket);
	}

	/**
	 * Schickt eine Chat-Nachricht an den Server. Dieser wird sie
	 * schnellstmoeglich an alle Clients weiterleiten (darunter auch dieser
	 * Client selbst).
	 **/
	void chat(String text) {
		/* Bei Textlaenge von 0 wird nix verschickt */
		if (text.length() < 1)return;
		
		new NET_CHAT(text, -1).send(client_socket);
	}
	
	public String getLastHost() {
		return lastHost;
	}
	
	public void send(NET_HEADER msg) {
		msg.send(client_socket);
	}
}
