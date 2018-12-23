package de.saschahlusiak.freebloks.game;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class BluetoothClientBridge extends Thread {
	private BluetoothSocket remote;
	private Socket local;
	private String hostname;
	private int port;

	private final Runnable socketReader = new Runnable() {
		@Override
		public void run() {
			try {
				InputStream is = local.getInputStream();
				OutputStream os = remote.getOutputStream();

				byte buffer[] = new byte[8192];
				int read;
				while ((read = is.read(buffer)) > 0) {
					os.write(buffer, 0, read);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			// close both connections
			try {
				if (local != null) local.close();
				local = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if (remote != null) remote.close();
				remote = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};

	private final Runnable bluetoothReader = new Runnable() {
		@Override
		public void run() {
			try {
				InputStream is = remote.getInputStream();
				OutputStream os = local.getOutputStream();

				byte buffer[] = new byte[8192];
				int read;
				while ((read = is.read(buffer)) > 0) {
					os.write(buffer, 0, read);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			// close both connections
			try {
				if (local != null) local.close();
				local = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if (remote != null) remote.close();
				remote = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};

	public BluetoothClientBridge(BluetoothSocket remote, String host, int port) {
		super("BluetoothClientBridge");
		this.remote = remote;
		this.hostname = host;
		this.port = port;
	}

	@Override
	public void run() {
		try {
			final SocketAddress localAddress = new InetSocketAddress(hostname, port);
			local = new Socket();
			local.connect(localAddress, 3000);
		} catch (IOException e) {
			e.printStackTrace();
			try {
				remote.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return;
		}

		Thread bluetoothThread = new Thread(bluetoothReader, "BluetoothReader");
		Thread socketThread = new Thread(socketReader, "SocketReader");

		bluetoothThread.start();
		socketThread.start();

		try {
			socketThread.join();
			bluetoothThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		try {
			if (remote != null)
				remote.close();
			if (local != null)
				local.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
