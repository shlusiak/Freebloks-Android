package de.saschahlusiak.freebloks.lobby;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.controller.SpielClient;
import de.saschahlusiak.freebloks.controller.SpielClientInterface;
import de.saschahlusiak.freebloks.model.Spiel;
import de.saschahlusiak.freebloks.model.Stone;
import de.saschahlusiak.freebloks.model.Turn;
import de.saschahlusiak.freebloks.network.NET_CHAT;
import de.saschahlusiak.freebloks.network.NET_SERVER_STATUS;
import de.saschahlusiak.freebloks.network.NET_SET_STONE;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout.LayoutParams;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class LobbyDialog extends Dialog implements SpielClientInterface {
	SpielClient client;
	Handler handler = new Handler();
	ListView chatList;
	ArrayAdapter<ChatEntry> adapter;
	NET_SERVER_STATUS lastStatus = null;
	GridView colorGrid;
	ColorAdapter colorAdapter;

	public LobbyDialog(Context context, OnCancelListener cancelListener, ArrayList<ChatEntry> chatEntries) {
		super(context, true, cancelListener);
		setContentView(R.layout.lobby_dialog);

		/* to make sure we have enough real estate. not neccessary on xlarge displays */
		if ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) != 
		        Configuration.SCREENLAYOUT_SIZE_XLARGE) {
			getWindow().setLayout(LayoutParams.FILL_PARENT,	LayoutParams.WRAP_CONTENT);
		}
		
		colorGrid = (GridView)findViewById(R.id.color_grid);
		colorAdapter = new ColorAdapter(getContext(), null, null);
		colorGrid.setAdapter(colorAdapter);

		findViewById(R.id.startButton).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				LobbyDialog.this.client.request_start();
			}
		});
		findViewById(R.id.chatButton).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				sendChat();
			}
		});
		((EditText)findViewById(R.id.chatText)).setOnEditorActionListener(new OnEditorActionListener() {			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_NULL) {
					sendChat();
					return true;
				}
				return false;
			}
		});

		adapter = new ChatListAdapter(getContext(), chatEntries);
		chatList = (ListView)findViewById(R.id.chatList);
		chatList.setAdapter(adapter);
		
		if (getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
	}

	@Override
	public Bundle onSaveInstanceState() {
		Bundle b = super.onSaveInstanceState();
		b.putSerializable("lastStatus", lastStatus);
		return b;
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		NET_SERVER_STATUS status = (NET_SERVER_STATUS)savedInstanceState.getSerializable("lastStatus");
		if (status != null) {
			lastStatus = status;
			updateStatus();
		}
		super.onRestoreInstanceState(savedInstanceState);
	}	
	
	public void setSpiel(SpielClient client) {
		this.client = client;
		lastStatus = null;
		/* what do we do if supplied client is null? */
		if (client == null)
			return;
		
		client.addClientInterface(this);
		if (client.spiel.current_player() < 0) {
			/* lobby */
			findViewById(R.id.startButton).setVisibility(View.VISIBLE);
			setTitle(R.string.lobby_waiting_for_players);
		} else {
			/* chat */
			findViewById(R.id.startButton).setVisibility(View.GONE);
			setTitle(R.string.chat);
		}

		TextView server = (TextView)findViewById(R.id.server);
		if (client.getLastHost() == null) {
			try {
				String s = null;
				Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
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
							if (s == null)
								s = addr.getHostAddress();
							else
								s += "\n" + addr.getHostAddress();
						}
					}
				if (s == null) /* no address found, clients will not be able to connect */
					throw new SocketException("no address found");
				server.setText(s);
				server.setTypeface(Typeface.DEFAULT_BOLD);
				server.setTextColor(Color.WHITE);
			} catch (SocketException e) {
				e.printStackTrace();
				server.setText(R.string.no_addresses_found);
				server.setTypeface(Typeface.DEFAULT);
				server.setTextColor(Color.RED);
			}
		} else {
			server.setTypeface(Typeface.DEFAULT);
			server.setText(client.getLastHost());
		}
		
		updateStatus();
	}
	
	void sendChat() {
		EditText edit = (EditText)findViewById(R.id.chatText);
		String s = edit.getText().toString();
		if (s.length() < 1)
			return;
		
		NET_CHAT chat = new NET_CHAT(s + "\n");
		LobbyDialog.this.client.send(chat);
		edit.setText("");
	}
	
	@Override
	protected void onStop() {
		if (client != null)
			client.removeClientInterface(this);
		super.onStop();
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
	public void chatReceived(final NET_CHAT c) {
		chatList.post(new Runnable() {
			@Override
			public void run() {
				adapter.notifyDataSetChanged();
			}
		});
	}
	
	@Override
	public void gameStarted() {
		dismiss();
	}

	@Override
	public void stoneUndone(Stone s, Turn t) {
		
	}

	@Override
	public void serverStatus(final NET_SERVER_STATUS status) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				lastStatus = status;
				updateStatus();
			}
		});
	}
	
	void updateStatus() {
		/* better: dismiss */
		if (client == null)
			return;
		colorAdapter.setCurrentStatus(client.spiel, lastStatus);
		
		if (lastStatus == null) {			
			findViewById(R.id.clients).setVisibility(View.INVISIBLE);
		} else {
			findViewById(R.id.clients).setVisibility(View.VISIBLE);
			((TextView)findViewById(R.id.clients)).setText(getContext().getString(R.string.connected_clients, lastStatus.clients));
		}
	}

	@Override
	public void onConnected(Spiel spiel) {
		
	}

	@Override
	public void onDisconnected(Spiel spiel) {
		dismiss();
	}
}
