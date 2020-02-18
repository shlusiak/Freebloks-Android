package de.saschahlusiak.freebloks.client;

import android.bluetooth.BluetoothSocket;
import android.content.Context;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;

import com.crashlytics.android.Crashlytics;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.game.GameConfiguration;
import de.saschahlusiak.freebloks.model.Board;
import de.saschahlusiak.freebloks.model.Game;
import de.saschahlusiak.freebloks.model.GameMode;
import de.saschahlusiak.freebloks.model.Turn;
import de.saschahlusiak.freebloks.network.MessageReadThread;
import de.saschahlusiak.freebloks.network.MessageWriter;
import de.saschahlusiak.freebloks.network.message.MessageChat;
import de.saschahlusiak.freebloks.network.message.MessageRequestGameMode;
import de.saschahlusiak.freebloks.network.message.MessageRequestHint;
import de.saschahlusiak.freebloks.network.message.MessageRequestPlayer;
import de.saschahlusiak.freebloks.network.message.MessageRequestUndo;
import de.saschahlusiak.freebloks.network.message.MessageRevokePlayer;
import de.saschahlusiak.freebloks.network.message.MessageSetStone;
import de.saschahlusiak.freebloks.network.message.MessageStartGame;
import io.fabric.sdk.android.Fabric;

public class GameClient {
	private static final int CONNECT_TIMEOUT = 10000;
	public static final int DEFAULT_PORT = 59995;

	private Closeable clientSocket;
	public final Game game;
	public final Board board;
	private MessageWriter messageWriter;
	private MessageReadThread readThread;
	private ExecutorService sendExecutor;
	private GameConfiguration config;
	private GameClientMessageHandler gameClientMessageHandler;

	@UiThread
	public GameClient(@Nullable Game game, @NonNull GameConfiguration config) {
		this.config = config;
		if (game == null) {
			Board board = new Board(config.getFieldSize());
			board.startNewGame(GameMode.GAMEMODE_4_COLORS_4_PLAYERS);
			board.setAvailableStones(0, 0, 0, 0, 0);

			this.game = new Game(board);
		} else
			this.game = game;

		this.board = this.game.getBoard();

		clientSocket = null;

		gameClientMessageHandler = new GameClientMessageHandler(this.game);
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
		if (clientSocket == null) return false;
		if (clientSocket instanceof Socket && ((Socket) clientSocket).isClosed()) return false;
		if (clientSocket instanceof BluetoothSocket && !((BluetoothSocket) clientSocket).isConnected()) return false;

		return true;
	}

	public void addObserver(GameEventObserver sci) {
		gameClientMessageHandler.addObserver(sci);
	}

	public void removeObserver(GameEventObserver sci) {
		gameClientMessageHandler.removeObserver(sci);
	}

	/**
	 * Try to establish a TCP connection to the given host and port.
	 * On success will call {@link #connected(Closeable, InputStream, OutputStream)}
	 *
	 * @param context for getStrin()
	 * @param host target hostname
	 * @param port port
	 *
	 * @throws IOException on connection refused
	 */
	@WorkerThread
	public void connect(Context context, String host, int port) throws IOException {
		final Socket socket = new Socket();
		try {
			SocketAddress address;
			if (host == null)
				address = new InetSocketAddress((InetAddress)null, port);
			else
				address = new InetSocketAddress(host, port);

			socket.connect(address, CONNECT_TIMEOUT);
		} catch (IOException e) {
			// translate any IOException to "Connection refused"
			e.printStackTrace();

			throw new IOException(context.getString(R.string.connection_refused));
		}

		connected(socket, socket.getInputStream(), socket.getOutputStream());
	}

	/**
	 * Connection is successful, set up message readers and writers.
	 * Make sure you have observers registered before calling this method.
	 *
	 * @param socket a closeable socket, for disconnecting
	 * @param is the InputStream from the socket
	 * @param os the OutputStream to the socket
	 */
	public synchronized void connected(@NonNull Closeable socket, @NonNull InputStream is, @NonNull OutputStream os) {
		this.clientSocket = socket;

		// first we set up writing to the server
		messageWriter = new MessageWriter(os);
		sendExecutor = Executors.newSingleThreadExecutor();

		// we start reading from the server, we will likely begin receiving and processing
		// messages and sending events to the observers.
		// To allow for other observers to be registered before we consume messages, we inform the
		// observers so far about it.
		gameClientMessageHandler.onConnected(this);

		readThread = new MessageReadThread(is, gameClientMessageHandler, () -> {
			disconnect();
			return null;
		});
		readThread.start();
	}

	public synchronized void disconnect() {
		if (clientSocket != null) {
			final Exception lastError = readThread.getError();
			try {
				if (Fabric.isInitialized()) {
					Crashlytics.log("Disconnecting from " + config.getServer());
				}
				readThread.goDown();

				if (clientSocket instanceof Socket) {
					final Socket socket = (Socket) clientSocket;
					if (socket.isConnected())
						socket.shutdownInput();
				}

				if (sendExecutor != null) {
					sendExecutor.shutdown();
					try {
						sendExecutor.awaitTermination(200, TimeUnit.MILLISECONDS);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				clientSocket.close();
				readThread = null;
				sendExecutor = null;
			} catch (IOException e) {
				e.printStackTrace();
			}

			gameClientMessageHandler.onDisconnected(this, lastError);
		}
		clientSocket = null;
	}

	/**
	 * Request a new player from the server
	 * @param player player to request
	 * @param name name for the player
	 */
	public void requestPlayer(int player, @Nullable String name) {
		// be aware that the name may be capped at length 16
		send(new MessageRequestPlayer(player, name));
	}

	/**
	 * Request to revoke the given player
	 * @param player the local player to revoke
	 */
	public void revokePlayer(int player) {
		if (!game.isLocalPlayer(player)) return;
		send(new MessageRevokePlayer(player));
	}

	/**
	 * Request a new game mode with new board sizes from the server.
	 *
	 * @param width new width to request
	 * @param height new height to request
	 * @param gameMode new game mode to request
	 * @param stones availability of the 21 stones
	 */
	public void requestGameMode(int width, int height, @NonNull GameMode gameMode, @NonNull int[] stones) {
		send(new MessageRequestGameMode(width, height, gameMode, stones));
	}

	/**
	 * Request a hint for the current local player.
	 */
	public void requestHint() {
		if (game == null)
			return;
		if (!isConnected())
			return;
		if (!game.isLocalPlayer())
			return;
		send(new MessageRequestHint(game.getCurrentPlayer()));
	}

	/**
	 * Send a chat message to the server, which will relay it back
	 * @param message the message
	 */
	public void sendChat(@NonNull String message) {
		// the client does not matter, it will be filled in by the server then broadcasted to all clients
		send(new MessageChat(0, message));
	}

	/**
	 * Called by the UI for the local player to place the stone.
	 * The request is sent to the server, the stone will not be placed locally.
	 */
	public void setStone(@NonNull Turn turn) {
		// locally set no player as the current player
		// on success the server will send us the new current player
		game.setCurrentPlayer(-1);

		send(new MessageSetStone(turn));
	}

	/**
	 * Request server to start the game
	 */
	public void requestGameStart() {
		send(new MessageStartGame());
	}

	/**
	 * Request server to undo the last move
	 */
	public void requestUndo() {
		if (game == null)
			return;
		if (!isConnected())
			return;
		if (!game.isLocalPlayer())
			return;
		send(new MessageRequestUndo());
	}

	/**
	 * Relay the given message to the sendExecutor to be sent to the server asynchronously.
	 * Write errors will be silently ignored.
	 *
	 * @param msg the message to send
	 */
	@AnyThread
	private void send(@NonNull de.saschahlusiak.freebloks.network.Message msg) {
		if (sendExecutor == null)
			return;

		// ignore sending to closed stream
		sendExecutor.submit(() -> messageWriter.write(msg));
	}
}
