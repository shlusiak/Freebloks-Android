package de.saschahlusiak.freebloks.lobby;

import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.R.id;
import de.saschahlusiak.freebloks.R.layout;
import de.saschahlusiak.freebloks.R.string;
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
import android.graphics.Color;
import android.os.Handler;
import android.sax.TextElementListener;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

public class LobbyDialog extends Dialog implements SpielClientInterface {
	SpielClient spiel;
	Handler handler = new Handler();
	ListView chatList;
	ArrayAdapter<NET_CHAT> adapter;

	public LobbyDialog(Context context,
			OnCancelListener cancelListener) {
		super(context, true, cancelListener);
		setContentView(R.layout.lobby_dialog);

		getWindow().setLayout(LayoutParams.FILL_PARENT,	LayoutParams.WRAP_CONTENT);
		setTitle(R.string.lobby_waiting_for_players);
		
		findViewById(R.id.startButton).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				LobbyDialog.this.spiel.request_start();
			}
		});
		findViewById(R.id.chatButton).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				sendChat();
			}
		});

		adapter = new ChatListAdapter(getContext());
		chatList = (ListView)findViewById(R.id.chatList);
		chatList.setAdapter(adapter);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		adapter.clear();
	}
	
	public void setSpiel(SpielClient spiel) {
		this.spiel = spiel;
		spiel.addClientInterface(this);

	}
	
	void sendChat() {
		EditText edit = (EditText)findViewById(R.id.chatText);
		String s = edit.getText().toString();
		if (s.length() < 1)
			return;
		
		NET_CHAT chat = new NET_CHAT(s + "\n");
		LobbyDialog.this.spiel.send(chat);
		edit.setText("");
	}
	
	@Override
	protected void onStop() {
		spiel.removeClientInterface(this);
		super.onStop();
	}

	@Override
	public void newCurrentPlayer(int player) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stoneWasSet(NET_SET_STONE s) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hintReceived(NET_SET_STONE s) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void gameFinished() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void chatReceived(final NET_CHAT c) {
		chatList.post(new Runnable() {
			
			@Override
			public void run() {
				adapter.add(c);				
			}
		});
	}

	@Override
	public void gameStarted() {
		dismiss();
	}

	@Override
	public void stoneUndone(Stone s, Turn t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void serverStatus(final NET_SERVER_STATUS status) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				((TextView)findViewById(R.id.server)).setText("" + spiel.getLastHost());
				
				((TextView)findViewById(R.id.clients)).setText(getContext().getString(R.string.connected_clients, status.clients));
				
				/* TODO: Translate texts */
				TextView v = (TextView)findViewById(R.id.your_color);
				String colorNames[] = { "Blue","Yellow", "Red", "Green" };
				int colors[] = { Color.BLUE, Color.YELLOW, Color.RED , Color.GREEN };
				
				v.setText("None");
				v.setTextColor(Color.WHITE);
				for (int i = 0; i < Spiel.PLAYER_MAX; i++) if (spiel.spiel.is_local_player(i)) {
					v.setText(colorNames[i]);
					v.setTextColor(colors[i]);
				}
			}
		});
	}

	@Override
	public void onConnected(Spiel spiel) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDisconnected(Spiel spiel) {
		// TODO Auto-generated method stub
		
	}
}
