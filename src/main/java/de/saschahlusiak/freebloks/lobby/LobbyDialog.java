package de.saschahlusiak.freebloks.lobby;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import com.github.clans.fab.FloatingActionButton;
import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.controller.GameMode;
import de.saschahlusiak.freebloks.controller.SpielClient;
import de.saschahlusiak.freebloks.controller.GameObserver;
import de.saschahlusiak.freebloks.game.CustomGameDialog;
import de.saschahlusiak.freebloks.game.GameConfiguration;
import de.saschahlusiak.freebloks.model.Spiel;
import de.saschahlusiak.freebloks.model.Turn;
import de.saschahlusiak.freebloks.network.message.MessageServerStatus;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.FrameLayout.LayoutParams;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class LobbyDialog extends Dialog implements GameObserver, OnItemClickListener, OnItemSelectedListener {
	SpielClient client;
	Handler handler = new Handler();
	ListView chatList;
	ChatListAdapter adapter;
	MessageServerStatus lastStatus = null;
	GridView colorGrid;
	ColorAdapter colorAdapter;
	Spinner gameMode, fieldSize;
	FloatingActionButton chatButton;
	EditText chatText;

	public LobbyDialog(Context context, ArrayList<ChatEntry> chatEntries) {
		super(context);
		
		setCancelable(true);

		requestWindowFeature(Window.FEATURE_LEFT_ICON);
		
		setContentView(R.layout.lobby_dialog);

		setCanceledOnTouchOutside(false);

		setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.notification_waiting_large);

		/* to make sure we have enough real estate. not neccessary on xlarge displays */
		if ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) !=
		        Configuration.SCREENLAYOUT_SIZE_XLARGE) {
			getWindow().setLayout(LayoutParams.FILL_PARENT,	LayoutParams.WRAP_CONTENT);
		}

		colorGrid = (GridView)findViewById(R.id.color_grid);
		colorAdapter = new ColorAdapter(this, getContext(), null, null);
		colorGrid.setAdapter(colorAdapter);

		colorGrid.setOnItemClickListener(this);
		
		gameMode = (Spinner)findViewById(R.id.game_mode);
		gameMode.setSelection(GameMode.GAMEMODE_4_COLORS_4_PLAYERS.ordinal());
		gameMode.setEnabled(false);
		gameMode.setOnItemSelectedListener(this);

		fieldSize = (Spinner)findViewById(R.id.field_size);
		fieldSize.setSelection(4);
		fieldSize.setEnabled(false);
		fieldSize.setOnItemSelectedListener(this);


		findViewById(R.id.startButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				LobbyDialog.this.client.request_start();
			}
		});
		chatButton = (FloatingActionButton) findViewById(R.id.chatButton);
		chatButton.setEnabled(false);
		chatButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendChat(chatText.getText().toString());
			}
		});
		((EditText)findViewById(R.id.chatText)).setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_NULL) {
					sendChat(chatText.getText().toString());
					return true;
				}
				return false;
			}
		});
		chatText = (EditText)findViewById(R.id.chatText);
		chatText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				chatButton.setEnabled(s.length() > 0);
			}
		});

		adapter = new ChatListAdapter(getContext(), chatEntries, GameMode.GAMEMODE_4_COLORS_4_PLAYERS);
		chatList = (ListView)findViewById(R.id.chatList);
		chatList.setAdapter(adapter);

		if (getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		else
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
	}

	@Override
	public Bundle onSaveInstanceState() {
		Bundle b = super.onSaveInstanceState();
		b.putSerializable("lastStatus", lastStatus);
		return b;
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		MessageServerStatus status = (MessageServerStatus)savedInstanceState.getSerializable("lastStatus");
		if (status != null) {
			lastStatus = status;
			updateStatus();
		}
		super.onRestoreInstanceState(savedInstanceState);
	}

	public void editPlayerName(final int player) {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
		LayoutInflater inflater = this.getLayoutInflater();
		final View dialogView = inflater.inflate(R.layout.edit_name_dialog, null);
		dialogBuilder.setView(dialogView);

		final EditText edt = dialogView.findViewById(android.R.id.edit);

		edt.setText(lastStatus.getClientName(getContext().getResources(), lastStatus.getSpieler()[player]));

		dialogBuilder.setTitle(R.string.prefs_player_name);
		dialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				final String name = edt.getText().toString();

				PreferenceManager.getDefaultSharedPreferences(getContext())
					.edit()
					.putString("player_name", name)
					.apply();

				client.revoke_player(player);
				client.request_player(player, name);
			}
		});
		dialogBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				//pass
			}
		});
		AlertDialog b = dialogBuilder.create();
		b.show();

		edt.selectAll();
		edt.requestFocus();

		b.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		b.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
	}

	public void setSpiel(SpielClient client) {
		this.client = client;
		lastStatus = null;
		/* what do we do if supplied client is null? */
		if (client == null)
			return;

		client.addObserver(this);
		if (!client.spiel.isStarted()) {
			/* lobby */
			findViewById(R.id.startButton).setVisibility(View.VISIBLE);
			setTitle(R.string.lobby_waiting_for_players);
		} else {
			/* chat */
			findViewById(R.id.startButton).setVisibility(View.GONE);
			setTitle(R.string.chat);
		}

	//	TextView server = (TextView)findViewById(R.id.server);
		/* TODO: this is added again and again on screen rotate */
		if (client.getConfig().getServer() == null) {
			try {
				Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
				int n = 0;
				if (interfaces != null)
					while (interfaces.hasMoreElements()) {
						NetworkInterface i = interfaces.nextElement();
						Enumeration<InetAddress> addresses = i.getInetAddresses();
						while (addresses.hasMoreElements()) {
							InetAddress addr = addresses.nextElement();
							if (addr.isAnyLocalAddress())
								continue;
							if (addr.isLinkLocalAddress())
								continue;
							if (addr.isLoopbackAddress())
								continue;
							if (addr.isMulticastAddress())
								continue;
							String a = addr.getHostAddress();
							if (a.contains("%"))
								a = a.substring(0, a.indexOf("%"));
							adapter.add(new ChatEntry(-1, "[" + a + "]", null));
							n++;
						}
					}
				if (n == 0) /* no address found, clients will not be able to connect */
					throw new SocketException("no address found");
			//	server.setText(s);
			//	server.setTypeface(Typeface.DEFAULT_BOLD);
			//	server.setTextColor(Color.WHITE);
			} catch (SocketException e) {
				e.printStackTrace();
			//	server.setText(R.string.no_addresses_found);
			//	server.setTypeface(Typeface.DEFAULT);
			//	server.setTextColor(Color.RED);
			}
		} else {
		//	server.setTypeface(Typeface.DEFAULT);
		//	server.setText(client.getLastHost());
		}
		adapter.setGameMode(client.spiel.getGameMode());

		updateStatus();
		adapter.notifyDataSetChanged();
	}

	void sendChat(String text) {
		if (text.length() < 1)
			return;

		client.sendChat(text + "\n");
		chatText.setText("");
	}

	@Override
	protected void onStop() {
		if (client != null)
			client.removeObserver(this);
		super.onStop();
	}

	@Override
	public void newCurrentPlayer(int player) {

	}

	@Override
	public void stoneWillBeSet(@NonNull Turn turn) {

	}

	@Override
	public void stoneHasBeenSet(@NonNull Turn turn) {

	}

	@Override
	public void hintReceived(@NonNull Turn turn) {

	}

	@Override
	public void gameFinished() {

	}

	@Override
	public void chatReceived(int client, @NonNull String message) {
		chatList.postDelayed(new Runnable() {
			@Override
			public void run() {
				adapter.notifyDataSetChanged();
			}
		}, 100);
	}

	@Override
	public void gameStarted() {
		dismiss();
	}

	@Override
	public void stoneUndone(@NonNull Turn t) {

	}

	@Override
	public void serverStatus(@NonNull final MessageServerStatus status) {
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				lastStatus = status;
				adapter.setGameMode(client.spiel.getGameMode());
				updateStatus();
				adapter.notifyDataSetChanged();
			}
		}, 100);
	}

	void updateStatus() {
		/* better: dismiss */
		if (client == null)
			return;
		colorAdapter.setCurrentStatus(client.spiel, lastStatus);

		TextView clients = (TextView)findViewById(R.id.clients);
		if (clients != null) {
			if (lastStatus == null) {
				findViewById(R.id.clients).setVisibility(View.INVISIBLE);
			} else {
				findViewById(R.id.clients).setVisibility(View.VISIBLE);
				((TextView)findViewById(R.id.clients)).setText(getContext().getResources().getQuantityString(R.plurals.connected_clients, lastStatus.getClients(), lastStatus.getClients()));
			}
		}
		
		if (lastStatus != null) {
			gameMode.setSelection(lastStatus.getGameMode().ordinal());
			gameMode.setEnabled(!client.spiel.isStarted() && lastStatus.isVersion(3));
			
			int slider = 3;
			for (int i = 0; i < CustomGameDialog.FIELD_SIZES.length; i++)
				if (CustomGameDialog.FIELD_SIZES[i] == lastStatus.getWidth())
					slider = i;
			fieldSize.setSelection(slider);
			fieldSize.setEnabled(!client.spiel.isStarted() && lastStatus.isVersion(3));

		} else {
			gameMode.setEnabled(false);
			fieldSize.setEnabled(false);
		}
	}

	@Override
	public void onConnected(@NonNull Spiel spiel) {

	}

	@Override
	public void onDisconnected(@NonNull Spiel spiel) {
		// dismiss may stop the dialog and remove this instance from the client listeners, modifying the underlying
		// list, so we have do run the dismiss() outside of this method.
		handler.post(new Runnable()
		{
			@Override
			public void run()
			{
				dismiss();
			}
		});
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		if (client.spiel.isStarted())
			return;
		if (client.spiel.is_local_player((int)id)) {
			client.revoke_player((int)id);
		} else {
			client.request_player((int)id, null);
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		GameMode g = GameMode.from(gameMode.getSelectedItemPosition());
		if (lastStatus == null)
			return;
		int size = CustomGameDialog.FIELD_SIZES[fieldSize.getSelectedItemPosition()];
		
		client.request_game_mode(size, size, g, g == GameMode.GAMEMODE_JUNIOR ? GameConfiguration.JUNIOR_STONE_SET : GameConfiguration.DEFAULT_STONE_SET);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		
	}
}
