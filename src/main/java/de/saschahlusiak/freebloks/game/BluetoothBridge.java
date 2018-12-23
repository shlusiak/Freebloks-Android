package de.saschahlusiak.freebloks.game;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
import com.crashlytics.android.Crashlytics;
import de.saschahlusiak.freebloks.controller.SpielClient;
import de.saschahlusiak.freebloks.controller.SpielClientInterface;
import de.saschahlusiak.freebloks.model.Spiel;
import de.saschahlusiak.freebloks.model.Turn;
import de.saschahlusiak.freebloks.network.NET_CHAT;
import de.saschahlusiak.freebloks.network.NET_SERVER_STATUS;
import de.saschahlusiak.freebloks.network.NET_SET_STONE;

import java.io.IOException;
import java.util.UUID;

public class BluetoothBridge extends Thread implements SpielClientInterface {
	private final static String tag = BluetoothBridge.class.getSimpleName();

	private final static String SERVICE_UUID = "B4C72729-2E7F-48B2-B15C-BDD73CED0D13";

	private final SpielClient client;
	private final Context context;
	private final BluetoothAdapter bluetoothAdapter;
	private BluetoothServerSocket serverSocket;

	public BluetoothBridge(Context context, SpielClient client) {
		super("BluetoothServerBridge");

		this.client = client;
		this.context = context;

		client.addClientInterface(this);

		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		Log.i(tag, "name " + bluetoothAdapter.getName());
		Log.i(tag, "address " + bluetoothAdapter.getAddress());
		Log.i(tag, "enabled " + bluetoothAdapter.isEnabled());
	}

	public void run() {
		Log.i(tag, "starting");

		try {
			serverSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("freebloks", UUID.fromString(SERVICE_UUID));
			if (serverSocket == null) {
				Log.e(tag, "Failed to create server socket");
			}
		} catch (IOException e) {
			e.printStackTrace();
			Crashlytics.logException(e);
			return;
		}

		try {
			while (true) {
				BluetoothSocket clientSocket = serverSocket.accept();
				Log.i(tag, "client connected: " + clientSocket.getRemoteDevice().getName());


				// TODO: connect to localhost and establish duplex connection between sockets in background thread

				clientSocket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		stopListening();
		client.removeClientInterface(this);

		Log.i(tag, "stopping");
	}

	public void stopListening() {
		if (serverSocket == null)
			return;

		try {
			serverSocket.close();
			serverSocket = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void gameStarted() {
		stopListening();
	}

	@Override
	public void onConnected(Spiel spiel) {

	}

	@Override
	public void onDisconnected(Spiel spiel) {
		stopListening();
	}

	@Override
	public void newCurrentPlayer(int player) {

	}

	@Override
	public void stoneWillBeSet(NET_SET_STONE s) {

	}

	@Override
	public void stoneHasBeenSet(NET_SET_STONE s) {

	}

	@Override
	public void hintReceived(NET_SET_STONE s) {

	}

	@Override
	public void gameFinished() {

	}

	@Override
	public void chatReceived(NET_CHAT c) {

	}

	@Override
	public void stoneUndone(Turn t) {

	}

	@Override
	public void serverStatus(NET_SERVER_STATUS status) {

	}
}
