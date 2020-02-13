package de.saschahlusiak.freebloks.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;

import com.crashlytics.android.Crashlytics;
import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.game.GameConfiguration;
import de.saschahlusiak.freebloks.model.Spiel;
import de.saschahlusiak.freebloks.model.Turn;
import de.saschahlusiak.freebloks.network.*;
import de.saschahlusiak.freebloks.network.message.MessageChat;
import de.saschahlusiak.freebloks.network.message.MessageRequestHint;
import de.saschahlusiak.freebloks.network.message.MessageRequestPlayer;
import de.saschahlusiak.freebloks.network.message.MessageRequestUndo;
import de.saschahlusiak.freebloks.network.message.MessageRevokePlayer;
import de.saschahlusiak.freebloks.network.message.MessageSetStone;
import de.saschahlusiak.freebloks.network.message.MessageStartGame;

public class SpielClient {
	private static final String tag = SpielClient.class.getSimpleName();

	private static final int DEFAULT_TIMEOUT = 10000;
	public static final int DEFAULT_PORT = 59995;

	private Object client_socket;
	private OutputStream outputStream;
	public final Spielleiter spiel;
	private final MessageWriter messageWriter = new MessageWriter();
	private HandlerThread sendThread;
	private Handler sendHandler;
	private GameConfiguration config;
	private NetworkEventHandler networkEventHandler;
	private MessageReader reader;

	@UiThread
	public SpielClient(Spielleiter leiter, GameConfiguration config) {
		this.config = config;
		if (leiter == null) {
			spiel = new Spielleiter(config.getFieldSize());
			spiel.startNewGame(GameMode.GAMEMODE_4_COLORS_4_PLAYERS);
			spiel.setAvailableStones(0, 0, 0, 0, 0);
		} else
			this.spiel = leiter;
		client_socket = null;

		networkEventHandler = new NetworkEventHandler(spiel);
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

	public void addObserver(GameObserver sci) {
		networkEventHandler.addObserver(sci);
	}

	public void removeObserver(GameObserver sci) {
		networkEventHandler.removeObserver(sci);
	}

	@WorkerThread
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
			this.outputStream = socket.getOutputStream();
			sendThread = new HandlerThread("SendThread");
			sendThread.start();
			sendHandler = new Handler(sendThread.getLooper());

			networkEventHandler.onConnected();

			this.client_socket = socket;
		}
	}

	public void setSocket(BluetoothSocket socket) throws IOException {
		synchronized(this) {
			this.client_socket = socket;
			this.outputStream = socket.getOutputStream();

			sendThread = new HandlerThread("SendThread");
			sendThread.start();
			sendHandler = new Handler(sendThread.getLooper());

			networkEventHandler.onConnected();
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

			networkEventHandler.onDisconnected();
		}
		client_socket = null;
	}

	@WorkerThread
	public void poll() throws IOException {
		de.saschahlusiak.freebloks.network.Message msg;

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
			msg = reader.readMessage(is);
				
			if (msg != null)
				networkEventHandler.handleMessage(msg);
		}
		catch (GameStateException | ProtocolException e) {
			throw new RuntimeException(e);
		}
	}

	public void request_player(int player, @Nullable String name) {
		send(new MessageRequestPlayer(player, name));
	}

	public void revoke_player(int player) {
		send(new MessageRevokePlayer(player));
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
		send(new MessageRequestHint(player));
	}

	public void sendChat(String message) {
		// the client does not matter, it will be filled in by the server then broadcasted
		send(new MessageChat(0, message));
	}

	/**
	 * Wird von der GUI aufgerufen, wenn ein Spieler einen Stein setzen will Die
	 * Aktion wird nur an den Server geschickt, der Stein wird NICHT lokal
	 * gesetzt
	 **/
	public int set_stone(Turn turn)
	{
		if (spiel.m_current_player==-1)
			return Spiel.FIELD_DENIED;
		
		/* Lokal keinen Spieler als aktiv setzen.
	   	Der Server schickt uns nachher den neuen aktiven Spieler zu */
		spiel.set_noplayer();

		send(new MessageSetStone(turn));

		return Spiel.FIELD_ALLOWED;
	}

	/**
	 * Erbittet den Spielstart beim Server
	 **/
	public void request_start() {
		send(new MessageStartGame());
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
		send(new MessageRequestUndo());
	}

	@Deprecated
	private void send(NET_HEADER msg) {
		if (sendHandler == null)
			return;

		sendHandler.post(() -> msg.send(outputStream));
	}

	@AnyThread
	private void send(de.saschahlusiak.freebloks.network.Message msg) {
		if (sendHandler == null)
			return;

		sendHandler.post(() -> messageWriter.write(outputStream, msg));
	}
}
