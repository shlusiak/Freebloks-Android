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
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class ChatDialog extends Dialog {
	SpielClient client;
	ListView chatList;
	ArrayAdapter<ChatEntry> adapter;

	public ChatDialog(Context context, ArrayList<ChatEntry> chatEntries) {
		super(context);
				
		setContentView(R.layout.chat_dialog);

		setTitle(R.string.chat);
		/* FIXME: only do that on xlarge displays? */
	//	getWindow().setLayout(LayoutParams.FILL_PARENT,	LayoutParams.WRAP_CONTENT);
		setCanceledOnTouchOutside(true);
		
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
	
	public void setClient(SpielClient client) {
		this.client = client;
		adapter.notifyDataSetChanged();
	}
	
	public void chatReceived() {
		adapter.notifyDataSetChanged();
	}
	
	void sendChat() {
		EditText edit = (EditText)findViewById(R.id.chatText);
		String s = edit.getText().toString();
		if (s.length() < 1)
			return;
		
		NET_CHAT chat = new NET_CHAT(s + "\n");
		client.send(chat);
		edit.setText("");
	}
}
