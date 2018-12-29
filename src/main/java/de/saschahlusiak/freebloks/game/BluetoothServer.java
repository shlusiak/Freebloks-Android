package de.saschahlusiak.freebloks.game;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import com.crashlytics.android.Crashlytics;
import de.saschahlusiak.freebloks.controller.SpielClient;
import de.saschahlusiak.freebloks.controller.SpielClientInterface;
import de.saschahlusiak.freebloks.model.Spiel;
import de.saschahlusiak.freebloks.model.Turn;
import de.saschahlusiak.freebloks.network.NET_CHAT;
import de.saschahlusiak.freebloks.network.NET_SERVER_STATUS;
import de.saschahlusiak.freebloks.network.NET_SET_STONE;
import de.saschahlusiak.freebloks.network.Network;

import java.io.IOException;
import java.util.UUID;

public class BluetoothServer extends Thread implements SpielClientInterface {
	private final static String tag = BluetoothServer.class.getSimpleName();

	public final static UUID SERVICE_UUID = UUID.fromString("B4C72729-2E7F-48B2-B15C-BDD73CED0D13");

	private final BluetoothAdapter bluetoothAdapter;
	private BluetoothServerSocket serverSocket;
	private OnBluetoothConnectedListener listener;
	private Handler handler = new Handler();

	public interface OnBluetoothConnectedListener {
		void onBluetoothClientConnected(BluetoothSocket socket);
	}

	public BluetoothServer(OnBluetoothConnectedListener listener) {
		super("BluetoothServerBridge");

		this.listener = listener;

		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		if (bluetoothAdapter != null) {
			try {
				Log.i(tag, "name " + bluetoothAdapter.getName());
				Log.i(tag, "address " + bluetoothAdapter.getAddress());
				Log.i(tag, "enabled " + bluetoothAdapter.isEnabled());
			} catch (SecurityException e) {
				// doesn't matter, but is interesting
				e.printStackTrace();
				Crashlytics.logException(e);
			}
		}
	}

	public void run() {
		if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
			Log.w(tag, "Bluetooth disabled, not starting bridge");
			return;
		}

		Log.i(tag, "Starting Bluetooth server");

		try {
			serverSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("freebloks", SERVICE_UUID);
			if (serverSocket == null) {
				Log.e(tag, "Failed to create server socket");
			}
		} catch (IOException | SecurityException e) {
			e.printStackTrace();
			Crashlytics.logException(e);
			return;
		}

		try {
			while (true) {
				final BluetoothSocket clientSocket = serverSocket.accept();
				Log.i(tag, "client connected: " + clientSocket.getRemoteDevice().getName());

				if (serverSocket == null || isInterrupted()) {
					clientSocket.close();
					break;
				}

				handler.post(new Runnable() {
					@Override
					public void run() {
						listener.onBluetoothClientConnected(clientSocket);
					}
				});
			}
		} catch (IOException e) {
			// nop
		}

		shutdown();

		Log.i(tag, "Stopping Bluetooth server");
	}

	public synchronized void shutdown() {
		if (serverSocket == null)
			return;

		try {
			serverSocket.close();
			interrupt();
			serverSocket = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onConnected(@NonNull Spiel spiel) {

	}

	@Override
	public void onDisconnected(@NonNull Spiel spiel) {
		shutdown();
	}

	@Override
	public void newCurrentPlayer(int player) {

	}

	@Override
	public void stoneWillBeSet(@NonNull NET_SET_STONE s) {

	}

	@Override
	public void stoneHasBeenSet(@NonNull NET_SET_STONE s) {

	}

	@Override
	public void hintReceived(@NonNull NET_SET_STONE s) {

	}

	@Override
	public void gameFinished() {

	}

	@Override
	public void chatReceived(@NonNull NET_CHAT c) {

	}

	@Override
	public void gameStarted() {
		shutdown();
	}

	@Override
	public void stoneUndone(@NonNull Turn t) {

	}

	@Override
	public void serverStatus(@NonNull NET_SERVER_STATUS status) {

	}
}
