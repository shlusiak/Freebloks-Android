package de.saschahlusiak.freebloks.lobby;

import java.util.ArrayList;

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
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethod;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class LobbyDialog extends Dialog implements SpielClientInterface {
	SpielClient spiel;
	Handler handler = new Handler();
	ListView chatList;
	ArrayAdapter<ChatEntry> adapter;
	ArrayList<ChatEntry> chatEntries;
	NET_SERVER_STATUS lastStatus = null;

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

		chatEntries = new ArrayList<ChatEntry>();
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
		b.putSerializable("chatEntries", chatEntries);
		return b;
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		NET_SERVER_STATUS status = (NET_SERVER_STATUS)savedInstanceState.getSerializable("lastStatus");
		if (status != null) {
			lastStatus = status;
			updateStatus();
		}
		ArrayList<ChatEntry> entries = (ArrayList<ChatEntry>)savedInstanceState.getSerializable("chatEntries");
		if (entries != null) {
			chatEntries.clear();
			chatEntries.addAll(entries);
			adapter.notifyDataSetChanged();
		}
		super.onRestoreInstanceState(savedInstanceState);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
	}
	
	public void setSpiel(SpielClient spiel) {
		this.spiel = spiel;
		adapter.clear();
		lastStatus = null;
		if (spiel != null)
			spiel.addClientInterface(this);
		updateStatus();
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
		if (spiel != null)
			spiel.removeClientInterface(this);
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
				/* TODO: if there is a client without a name, try to get the name of the
				 * first assigned color rather than the client name
				 * 
				 * i.e. "Blue" instead of "Client 0"
				 */
				ChatEntry e = new ChatEntry(c.client, c.text, getClientName(c.client));
				chatEntries.add(e);
				adapter.notifyDataSetChanged();
			}
		});
	}
	
	String getClientName(int client) {
		if (lastStatus == null)
			return getContext().getString(R.string.client_d, client);
		return lastStatus.getClientName(getContext().getResources(), client);
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
		((TextView)findViewById(R.id.server)).setText("" + spiel.getLastHost());
		
		LinearLayout l = (LinearLayout)findViewById(R.id.colors);
		final String colorNames[] = getContext().getResources().getStringArray(R.array.color_names);
		final int colors[] = { Color.BLUE, Color.YELLOW, Color.RED , Color.GREEN };
		
		/* for some reason removeAllViews does not remove running animations */
		for (int i = 0; i < l.getChildCount(); i++)
			l.getChildAt(i).clearAnimation();
		l.removeAllViews();
		if (lastStatus == null) {			
			findViewById(R.id.clients).setVisibility(View.INVISIBLE);
			findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
			
			findViewById(R.id.clients).setVisibility(View.VISIBLE);
			((TextView)findViewById(R.id.clients)).setText(getContext().getString(R.string.connected_clients, lastStatus.clients));
			if (lastStatus.isAdvanced()) {
				findViewById(R.id.textView8).setVisibility(View.GONE);
				TextView v;
				for (int i = 0; i < lastStatus.spieler.length; i++) if (lastStatus.spieler[i] >= 0) {
					v = new TextView(getContext());
					if (lastStatus.client_names[lastStatus.spieler[i]] == null)
						v.setText(getContext().getString(R.string.client_d, lastStatus.spieler[i]));
					else
						v.setText(lastStatus.client_names[lastStatus.spieler[i]]);
					v.setTextColor(colors[i]);
					v.setPadding(12, 0, 0, 0);
					l.addView(v);
					if (spiel.spiel.is_local_player(i)) {
						v.setTypeface(Typeface.DEFAULT_BOLD);
						
						Animation a = new TranslateAnimation(
								TranslateAnimation.RELATIVE_TO_SELF, 
								0, 
								TranslateAnimation.RELATIVE_TO_SELF, 
								0, 
								TranslateAnimation.RELATIVE_TO_SELF, 
								0, 
								TranslateAnimation.RELATIVE_TO_SELF, 
								-0.25f);
						a.setDuration(400);
						a.setInterpolator(new DecelerateInterpolator());
						a.setRepeatMode(Animation.REVERSE);
						a.setRepeatCount(Animation.INFINITE);

						v.startAnimation(a);
					}
				}
			} else {
				TextView v;
				findViewById(R.id.textView8).setVisibility(View.VISIBLE);
				for (int i = 0; i < Spiel.PLAYER_MAX; i++) if (spiel.spiel.is_local_player(i)) {
					v = new TextView(getContext());
					v.setText(colorNames[i]);
					v.setTextColor(colors[i]);
					v.setPadding(8, 0, 0, 0);
					l.addView(v);
				}
				if (l.getChildCount() <= 0) {
					v = new TextView(getContext());
					v.setText(R.string.lobby_no_color);
					v.setTextColor(Color.WHITE);
					l.addView(v);
				}
			}
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
