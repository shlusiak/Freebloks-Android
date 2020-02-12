package de.saschahlusiak.freebloks.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.crashlytics.android.Crashlytics;
import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.game.GameConfiguration;
import de.saschahlusiak.freebloks.model.Spiel;
import de.saschahlusiak.freebloks.model.Turn;
import de.saschahlusiak.freebloks.network.*;

public class SpielClient {
	static final String tag = SpielClient.class.getSimpleName();
	static final int DEFAULT_TIMEOUT = 10000;

	private final ArrayList<SpielClientInterface> spielClientInterface = new ArrayList<>();
	private Object client_socket;
	public final Spielleiter spiel;
	private HandlerThread sendThread;
	private Handler sendHandler;
	private GameConfiguration config;
	private NetworkMessageProcessor processor;
	private MessageReader reader;

	public SpielClient(Spielleiter leiter, GameConfiguration config) {
		this.config = config;
		if (leiter == null) {
			spiel = new Spielleiter(config.getFieldSize());
			spiel.startNewGame(GameMode.GAMEMODE_4_COLORS_4_PLAYERS);
			spiel.setAvailableStones(0, 0, 0, 0, 0);
		} else
			this.spiel = leiter;
		client_socket = null;

		processor = new NetworkMessageProcessor(spiel, spielClientInterface);
		reader = new MessageReader();
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
		if (client_socket == null) return false;
		if (client_socket instanceof Socket && ((Socket)client_socket).isClosed()) return false;
		if (client_socket instanceof BluetoothSocket && !((BluetoothSocket)client_socket).isConnected()) return false;

		return true;
	}

	public synchronized void addClientInterface(SpielClientInterface sci) {
		this.spielClientInterface.add(sci);
	}

	public synchronized void removeClientInterface(SpielClientInterface sci) {
		this.spielClientInterface.remove(sci);
	}

	public void connect(Context context, String host, int port) throws IOException {
		final Socket socket = new Socket();
		try {
			SocketAddress address;
			if (host == null)
				address = new InetSocketAddress((InetAddress)null, port);
			else
				address = new InetSocketAddress(host, port);
			
			socket.connect(address, DEFAULT_TIMEOUT);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException(context.getString(R.string.connection_refused));
		}
		synchronized(this) {
			sendThread = new HandlerThread("SendThread");
			sendThread.start();
			sendHandler = new SendHandler(socket.getOutputStream(), sendThread.getLooper());

			for (SpielClientInterface sci : spielClientInterface)
				sci.onConnected(spiel);

			this.client_socket = socket;
		}
	}

	public void setSocket(BluetoothSocket socket) throws IOException {
		synchronized(this) {
			this.client_socket = socket;

			sendThread = new HandlerThread("SendThread");
			sendThread.start();
			sendHandler = new SendHandler(socket.getOutputStream(), sendThread.getLooper());

			for (SpielClientInterface sci : spielClientInterface)
				sci.onConnected(spiel);
		}
	}

	public synchronized void disconnect() {
		if (client_socket != null) {
			try {
				Crashlytics.log("Disconnecting from " + config.getServer());
				if (client_socket instanceof Socket) {
					Socket socket = (Socket) client_socket;
					if (socket.isConnected())
						socket.shutdownInput();
					socket.close();
				}
				if (client_socket instanceof BluetoothSocket) {
					BluetoothSocket socket = (BluetoothSocket) client_socket;
					socket.close();
				}
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
		de.saschahlusiak.freebloks.network.Message pkg;

		try {
			final InputStream is;

			synchronized (this) {
				if (client_socket == null)
					return;
				if (client_socket instanceof Socket && !((Socket)client_socket).isInputShutdown())
					is = ((Socket)client_socket).getInputStream();
				else if (client_socket instanceof BluetoothSocket)
					is = ((BluetoothSocket)client_socket).getInputStream();
				else
					return;
			}

			/* Read a complete network message into buffer */
			pkg = reader.readMessage(is);
				
			if (pkg != null)
				processor.processMessage(pkg);
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

	/**
	 * Wird von der GUI aufgerufen, wenn ein Spieler einen Stein setzen will Die
	 * Aktion wird nur an den Server geschickt, der Stein wird NICHT lokal
	 * gesetzt
	 **/
	public int set_stone(Turn turn)
	{
		NET_SET_STONE data;
		if (spiel.m_current_player==-1)
			return Spiel.FIELD_DENIED;
		
		/* Lokal keinen Spieler als aktiv setzen.
	   	Der Server schickt uns nachher den neuen aktiven Spieler zu */
		spiel.set_noplayer();

		data = new NET_SET_STONE();
		/* Datenstruktur mit Daten der Aktion fuellen */
		data.player=turn.getPlayer();
		data.stone=turn.getShape().getNumber();
		data.mirror_count=turn.getMirrorCount();
		data.rotate_count=turn.getRotationCount();
		data.x=turn.getX();
		data.y=turn.getY();

		/* Nachricht ueber den Stein an den Server schicken */
		send(data);

		return Spiel.FIELD_ALLOWED;
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
