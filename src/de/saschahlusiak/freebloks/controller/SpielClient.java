package de.saschahlusiak.freebloks.controller;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import android.util.Log;

import de.saschahlusiak.freebloks.model.Spiel;
import de.saschahlusiak.freebloks.model.Stone;
import de.saschahlusiak.freebloks.model.Turn;
import de.saschahlusiak.freebloks.network.*;

public class SpielClient extends Spielleiter {
	static final String tag = SpielClient.class.getSimpleName();
	
	ArrayList<SpielClientInterface> spielClientInterface = new ArrayList<SpielClientInterface>();
	Socket client_socket;
	String lastHost;

	public boolean isConnected() {
		return (client_socket != null) && (!client_socket.isClosed());
	}
	
	@Override
	protected void finalize() throws Throwable {
		disconnect();
		super.finalize();
	}

	public boolean is_local_player() {
		return is_local_player(m_current_player);
	}

	public SpielClient() {
		super(Spiel.DEFAULT_FIELD_SIZE_Y, Spiel.DEFAULT_FIELD_SIZE_X);
		start_new_game();
		client_socket = null;
		set_stone_numbers(0, 0, 0, 0, 0);
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



	public void connect(String host, int port) throws Exception {
		this.lastHost = host;
		try {
			client_socket = new Socket(host, port);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Connection refused");
		}
	}

	void setSocket(Socket client_socket) {
		this.client_socket = client_socket;
	}

	public synchronized void disconnect() {
		if (client_socket != null)
			try {
				client_socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		client_socket = null;
	}

	public boolean poll(boolean block) {
		NET_HEADER pkg;
		/* Puffer fuer eine Netzwerknachricht. */
		do {
			/* Lese eine Nachricht komplett aus dem Socket in buffer */
			try {
				pkg = Network.read_package(client_socket, block);
				if (pkg != null)
					process_message(pkg);
				if (block)
					return (pkg != null);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		} while (pkg != null);
		return true;
	}

	synchronized public void request_player() {
		new NET_REQUEST_PLAYER().send(client_socket);
	}

	synchronized void process_message(NET_HEADER data) throws Exception {
		int i;
		switch(data.msg_type)
		{
		/* Der Server gewaehrt dem Client einen lokalen Spieler */
		case Network.MSG_GRANT_PLAYER:
			i=((NET_GRANT_PLAYER)data).player;
			/* Merken, dass es sich bei i um einen lokalen Spieler handelt */
			spieler[i] = PLAYER_LOCAL;
			break;

		/* Der Server hat einen aktuellen Spieler festgelegt */
		case Network.MSG_CURRENT_PLAYER: 
			m_current_player=((NET_CURRENT_PLAYER)data).player;
			for (SpielClientInterface sci : spielClientInterface)
				sci.newCurrentPlayer(m_current_player);
			break;
				
		/* Nachricht des Servers ueber ein endgueltiges Setzen eines Steins auf das Feld */
		case Network.MSG_SET_STONE: {
			NET_SET_STONE s=(NET_SET_STONE)data;
			/* Entsprechenden Stein des Spielers holen */
			Stone stone=get_player(s.player).get_stone(s.stone);
			/* Stein in richtige Position drehen */
			stone.mirror_rotate_to(s.mirror_count, s.rotate_count);
			/* Stein aufs echte Spielfeld setzen */
			Log.d(tag, "player " + s.player + " stone " + stone.get_number() + " to (" + s.x + "," + s.y + ")");
			if ((is_valid_turn(stone, s.player, s.y, s.x) == Stone.FIELD_DENIED) ||
			   (super.set_stone(stone, s.player, s.y, s.x) != Stone.FIELD_ALLOWED))
			{	// Spiel scheint nicht mehr synchron zu sein
				// GAANZ schlecht!!
				throw new Exception("Game not in sync!");
			}
			/* Zug der History anhaengen */
			addHistory(s.player, stone, s.y, s.x);
			for (SpielClientInterface sci : spielClientInterface)
				sci.stoneWasSet(s);
			break;
		}
		
		case Network.MSG_STONE_HINT: {
			for (SpielClientInterface sci : spielClientInterface)
				sci.hintReceived((NET_SET_STONE)data);
			break;
		}

		/* Server hat entschlossen, dass das Spiel vorbei ist */
		case Network.MSG_GAME_FINISH: {
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
			if (status.width != m_field_size_x || status.height != m_field_size_y)
				set_field_size_and_new(status.height,status.width);
			{
				boolean changed=false;
				for (i = 0; i < Stone.STONE_SIZE_MAX; i++)
				{
					changed |= (status.stone_numbers[i] != status.stone_numbers[i]);
					status.stone_numbers[i]=status.stone_numbers[i];
				}
				if (changed)set_stone_numbers(status.stone_numbers[0],status.stone_numbers[1],status.stone_numbers[2],status.stone_numbers[3],status.stone_numbers[4]);
			}
			m_gamemode = status.gamemode;
			if (m_gamemode == GAMEMODE_4_COLORS_2_PLAYERS)
				set_teams(0, 2, 1, 3);
			if (m_gamemode==GAMEMODE_2_COLORS_2_PLAYERS)
			{
				for (int n = 0 ; n < Stone.STONE_COUNT_ALL_SHAPES; n++){
					get_player(1).get_stone(n).set_available(0);
					get_player(3).get_stone(n).set_available(0);
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
			start_new_game();
			/* Unbedingt history leeren. */
			if (history != null)
				history.delete_all_turns();

//			set_stone_numbers(status.stone_numbers[0],status.stone_numbers[1],status.stone_numbers[2],status.stone_numbers[3],status.stone_numbers[4]);
			if (m_gamemode==GAMEMODE_4_COLORS_2_PLAYERS)
				set_teams(0,2,1,3);
			if (m_gamemode==GAMEMODE_2_COLORS_2_PLAYERS)
			{
				for (int n = 0 ; n < Stone.STONE_COUNT_ALL_SHAPES; n++){
					get_player(1).get_stone(n).set_available(0);
					get_player(3).get_stone(n).set_available(0);
				}
			}
			m_current_player=-1;
			for (SpielClientInterface sci : spielClientInterface)
				sci.gameStarted();
			break;
		}
	
		/* Server laesst den letzten Zug rueckgaengig machen */
		case Network.MSG_UNDO_STONE: {
			Turn t = history.m_tail;
			Stone stone = get_player(t.m_playernumber).get_stone(t.m_stone_number);
			for (SpielClientInterface sci : spielClientInterface)
				sci.stoneUndone(stone, t);
			undo_turn(history);
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
		if (m_current_player==-1)return Stone.FIELD_DENIED;

		data = new NET_SET_STONE();
		/* Datenstruktur mit Daten der Aktion fuellen */
		data.player=m_current_player;
		data.stone=stone.get_number();
		data.mirror_count=stone.get_mirror_counter();
		data.rotate_count=stone.get_rotate_counter();
		data.x=x;
		data.y=y;

		
		/* Nachricht ueber den Stein an den Server schicken */
		data.send(client_socket);

		/* Lokal keinen Spieler als aktiv setzen.
	   	Der Server schickt uns nachher den neuen aktiven Spieler zu */
		set_noplayer();
		return Stone.FIELD_ALLOWED;
	}

	/**
	 * Erbittet den Spielstart beim Server
	 **/
	public void request_start()
	{
		new NET_START_GAME().send(client_socket);
	}

	/**
	 * Erbittet eine Zugzuruecknahme beim Server
	 **/
	void request_undo() {
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
		return (spieler[player] != PLAYER_COMPUTER);
	}
	
	public String getLastHost() {
		return lastHost;
	}
	
	public void send(NET_HEADER msg) {
		msg.send(client_socket);
	}
}
