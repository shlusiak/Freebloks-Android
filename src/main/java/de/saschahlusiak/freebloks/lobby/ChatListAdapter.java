package de.saschahlusiak.freebloks.lobby;

import java.util.List;

import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.controller.GameMode;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ChatListAdapter extends ArrayAdapter<ChatEntry> {
	GameMode gamemode;

	public ChatListAdapter(Context context, List<ChatEntry> values, GameMode gamemode) {
		super(context, R.layout.chat_list_item, R.id.textView, values);
		setGameMode(gamemode);
	}

	public void setGameMode(GameMode gamemode) {
		this.gamemode = gamemode;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ChatEntry c = getItem(position);
		TextView t;

		View view = super.getView(position, convertView, parent);

		t = (TextView)view.findViewById(R.id.textView);
		if (c.client < 0) {
			t.setTextColor(Color.LTGRAY);
			t.setGravity(Gravity.RIGHT);
		} else {
			t.setTextColor(c.getColor(getContext(), gamemode));
			t.setGravity(Gravity.LEFT);
		}

		return view;
	}

}
