package de.saschahlusiak.freebloks.game;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.*;

import java.io.IOException;
import java.util.Set;

import de.saschahlusiak.freebloks.Global;
import de.saschahlusiak.freebloks.R;

public class JoinDialog extends Dialog implements RadioGroup.OnCheckedChangeListener, View.OnClickListener, TextWatcher, BluetoothServer.OnBluetoothConnectedListener {
	private static final String tag = JoinDialog.class.getSimpleName();

	private EditText name, server;
	private OnStartCustomGameListener listener;
	private RadioGroup serverType;
	private Button hostGame;

	private ViewGroup bluetoothList;
	private BluetoothAdapter bluetoothAdapter;
	private BluetoothServer bluetoothServer;

	private SharedPreferences prefs;

	public interface OnStartCustomGameListener {
		void setClientName(String name);
		void onJoinGame(String server);
		void onJoinGame(BluetoothSocket bluetooth);
		void onHostGame();
		void onHostBluetoothGameWithClient(BluetoothSocket clientSocket);
	}

	public JoinDialog(Context context, final OnStartCustomGameListener listener) {
		super(context, R.style.Theme_Freebloks_Light_Dialog);

		prefs = PreferenceManager.getDefaultSharedPreferences(context);

		this.listener = listener;

		setContentView(R.layout.join_dialog);

		setTitle(R.string.join_multiplayer_game);

		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		findViewById(android.R.id.closeButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		findViewById(android.R.id.button1).setOnClickListener(this);
		hostGame = findViewById(R.id.host_game);
		hostGame.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				saveSettings();
				dismiss();
				listener.setClientName(getName());
				listener.onHostGame();
			}
		});

		name = findViewById(R.id.name);
		server = findViewById(R.id.server_address);
		server.setText(prefs.getString("custom_server", ""));
		server.addTextChangedListener(this);

		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		bluetoothList = findViewById(R.id.bluetoothList);
		bluetoothList.setVisibility(View.GONE);
		if (bluetoothAdapter == null) {
			// bluetooth not supported
			findViewById(R.id.radioButton3).setVisibility(View.GONE);
		}

		serverType = findViewById(R.id.server_type);
		serverType.setOnCheckedChangeListener(this);
		((RadioButton)findViewById(R.id.radioButton1)).setChecked(true);
	}

	@Override
	protected void onStart() {
		super.onStart();

		startBluetoothServer();
	}

	@Override
	protected void onStop() {
		if (bluetoothServer != null) {
			bluetoothServer.shutdown();
			bluetoothServer = null;
		}
		super.onStop();
	}

	private void startBluetoothServer() {
		if (bluetoothServer == null && bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
			bluetoothServer = new BluetoothServer(this);
			bluetoothServer.start();
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		updateOkButtonEnabled();
	}

	@Override
	public void afterTextChanged(Editable s) {

	}

	private void updateOkButtonEnabled() {
		boolean enabled = true;
		int checkedId = serverType.getCheckedRadioButtonId();
		if (checkedId == R.id.radioButton2 && getCustomServer().isEmpty())
			enabled = false;

		if (checkedId == R.id.radioButton3)
			enabled = false;

		findViewById(android.R.id.button1).setEnabled(enabled);
	}

	@Override
	public void onClick(View view) {
		listener.setClientName(getName());
		switch (serverType.getCheckedRadioButtonId())
		{
			case R.id.radioButton1:
				listener.onJoinGame(Global.DEFAULT_SERVER_ADDRESS);
				break;
			case R.id.radioButton2:
				if (getCustomServer().isEmpty())
					return;

				listener.onJoinGame(getCustomServer());
				break;
		}
		saveSettings();
		dismiss();
	}

	private void saveSettings() {
		Editor editor = prefs.edit();
		editor.putString("player_name", getName());
		editor.putString("custom_server", getCustomServer());
		editor.apply();
	}

	public void setName(String name) {
		this.name.setText(name);
	}

	private String getName() {
		return name.getText().toString().trim();
	}

	private String getCustomServer() {
		return server.getText().toString().trim();
	}

	@Override
	public void onCheckedChanged(RadioGroup radioGroup, int i) {
		server.setVisibility(i == R.id.radioButton2 ? View.VISIBLE : View.GONE);
		hostGame.setVisibility((i == R.id.radioButton2 || i == R.id.radioButton3) ? View.VISIBLE : View.INVISIBLE);
		bluetoothList.setVisibility(i == R.id.radioButton3 ? View.VISIBLE : View.GONE);

		// wifi
		if (i == R.id.radioButton2) {
			server.requestFocus();
		}

		// bluetooth
		if (i == R.id.radioButton3) {
			startBluetoothServer();
			updateDeviceList();
		}
		updateOkButtonEnabled();
	}

	private class ConnectBluetoothTask extends AsyncTask<BluetoothDevice, Void, BluetoothSocket> {
		private ProgressDialog progress;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progress = new ProgressDialog(getContext());
			progress.setIndeterminate(true);
			progress.setMessage(getContext().getString(R.string.connecting));
			progress.setCancelable(true);
			progress.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					Log.w(tag, "Cancelling connect task");
					cancel(true);
				}
			});
			progress.show();
		}

		@Override
		protected BluetoothSocket doInBackground(BluetoothDevice... bluetoothDevices) {
			BluetoothDevice device = bluetoothDevices[0];
			BluetoothSocket socket;
			try {
				Log.i(tag, "Connecting to " + device.getName() + "/" + device.getAddress());
				socket = device.createInsecureRfcommSocketToServiceRecord(BluetoothServer.SERVICE_UUID);

				socket.connect();
				Log.i(tag, "Connection successful");
				return socket;
			} catch (IOException e) {
				Log.e(tag, "Connection failed");
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onCancelled() {
			progress.dismiss();
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(BluetoothSocket bluetoothSocket) {
			progress.dismiss();
			if (bluetoothSocket != null) {
				// success
				dismiss();
				listener.setClientName(getName());
				listener.onJoinGame(bluetoothSocket);
			} else {
				// error
				Toast.makeText(getContext(), R.string.connection_refused, Toast.LENGTH_LONG).show();
			}
			super.onPostExecute(bluetoothSocket);
		}
	}

	private void updateDeviceList() {
		final View.OnClickListener deviceSelectedListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				saveSettings();
				BluetoothDevice device = (BluetoothDevice) v.getTag();

				Log.i(tag, "Device selected: " + device.getName());
				new ConnectBluetoothTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, device);
			}
		};

		TextView t;

		bluetoothList.removeAllViews();
		Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
		Log.d(tag, "Paired devices: " + pairedDevices.size());

		if (!bluetoothAdapter.isEnabled() || pairedDevices.isEmpty()) {
			View v = getLayoutInflater().inflate(R.layout.join_bluetooth_device, bluetoothList, false);
			v.findViewById(R.id.image).setVisibility(View.GONE);
			t = v.findViewById(android.R.id.text1);
			t.setText(R.string.bluetooth_disabled);

			bluetoothList.addView(v, new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			return;
		}

		for (BluetoothDevice device: pairedDevices) {
			Log.d(tag, String.format("device %s [%s]", device.getName(), device.getAddress()));

			View v = getLayoutInflater().inflate(R.layout.join_bluetooth_device, bluetoothList, false);

			t = v.findViewById(android.R.id.text1);
			t.setText(device.getName());
			v.setTag(device);
			v.setOnClickListener(deviceSelectedListener);

			bluetoothList.addView(v, new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		}
	}

	@Override
	public void onBluetoothClientConnected(final BluetoothSocket socket) {
		// a client has connected to us. quickly host a game and get the two together

		dismiss();
		listener.setClientName(getName());
		listener.onHostBluetoothGameWithClient(socket);
	}
}
