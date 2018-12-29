package de.saschahlusiak.freebloks.game;

import de.saschahlusiak.freebloks.Global;
import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.controller.GameMode;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Spinner;

public class ColorListDialog extends Dialog implements OnItemClickListener, OnItemSelectedListener {
	private DialogInterface.OnClickListener listener;
	private AdapterView<ColorListAdapter> list;
	private ColorListAdapter adapter;
	private Spinner gameMode, fieldSize;
	
	public ColorListDialog(Context context, final DialogInterface.OnClickListener listener) {
		super(context, R.style.Theme_Freebloks_Light_Dialog);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.color_list_dialog);

		this.listener = listener;
		gameMode = (Spinner)findViewById(R.id.game_mode);
		gameMode.setOnItemSelectedListener(this);
		fieldSize = (Spinner)findViewById(R.id.field_size);
		fieldSize.setOnItemSelectedListener(this);
		
		adapter = new ColorListAdapter(getContext());
		// Can't have the same id for list and grid, otherwise rotate on Android 2.3 crashes
		// with class cast exception
		list = findViewById(android.R.id.list);
		if (list == null)
			list = findViewById(R.id.grid);
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);
		
		findViewById(R.id.random_button).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				listener.onClick(ColorListDialog.this, -1);
			}
		});
	}
	
	void setGameMode(GameMode gamemode) {
	//	setTitle(getContext().getResources().getStringArray(R.array.game_modes)[gamemode.ordinal()]);
		gameMode.setSelection(gamemode.ordinal());
		adapter.setGameMode(gamemode);
		
		switch (gamemode) {
		case GAMEMODE_2_COLORS_2_PLAYERS:
			fieldSize.setSelection(2);
			break;
		case GAMEMODE_DUO:
		case GAMEMODE_JUNIOR:
			fieldSize.setSelection(1);
			break;
		default:
			fieldSize.setSelection(4);
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		listener.onClick(this, (int)id);
	}
	

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		if (parent == gameMode)
			setGameMode(GameMode.from(position));
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		
	}

	public GameMode getGameMode() {
		return GameMode.from(gameMode.getSelectedItemPosition());
	}
	
	public int getBoardSize() {
		return CustomGameDialog.FIELD_SIZES[fieldSize.getSelectedItemPosition()];
	}


	static class ColorListAdapter extends ArrayAdapter<String> {
		String[] colors;
		GameMode gamemode;
		
		public ColorListAdapter(Context context) {
			super(context, R.layout.color_list_item, android.R.id.text1);

			colors = context.getResources().getStringArray(R.array.color_names);
			this.gamemode = null;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = super.getView(position, convertView, parent);
			View c = v.findViewById(R.id.color);

			LayerDrawable ld = (LayerDrawable)getContext().getResources().getDrawable(R.drawable.bg_card_1);
			ld.mutate();
			GradientDrawable item;
			
			item = ((GradientDrawable)ld.findDrawableByLayerId(R.id.shadow));
			int res = Global.PLAYER_BACKGROUND_COLOR_RESOURCE[Global.getPlayerColor((int)getItemId(position), gamemode)];
			item.setColor(getContext().getResources().getColor(res));

			item = ((GradientDrawable)ld.findDrawableByLayerId(R.id.color1));
			res = Global.PLAYER_FOREGROUND_COLOR_RESOURCE[Global.getPlayerColor((int)getItemId(position), gamemode)];
			item.setColor(getContext().getResources().getColor(res));
//			c.setBackgroundColor(Global.PLAYER_FOREGROUND_COLOR[Global.getPlayerColor((int)getItemId(position), gamemode)]);
			
			c.setBackgroundDrawable(ld);

			return v;
		}
		
		public void setGameMode(GameMode gamemode) {
			if (gamemode == this.gamemode)
				return;
			
			clear();
			
			switch (gamemode)
			{
			case GAMEMODE_2_COLORS_2_PLAYERS:
				add(colors[1]); // blue
				add(colors[3]); // red
				break;
			case GAMEMODE_DUO:
			case GAMEMODE_JUNIOR:
				add(colors[5]);
				add(colors[6]);
				break;
				
			case GAMEMODE_4_COLORS_2_PLAYERS:
			case GAMEMODE_4_COLORS_4_PLAYERS:
				add(colors[1]); // blue
				add(colors[2]); // Yellow
				add(colors[3]); // blue
				add(colors[4]); // green
				break;
				
			default:
				throw new RuntimeException("Whopsie, gamemode " + gamemode.ordinal() + " not implemented");
			}
			
			this.gamemode = gamemode;
			notifyDataSetChanged();
		}
		
		@Override
		public long getItemId(int position) {
			switch (gamemode) {
			case GAMEMODE_2_COLORS_2_PLAYERS:
			case GAMEMODE_DUO:
			case GAMEMODE_JUNIOR:
				if (position == 0)
					return 0;
				else
					return 2;
				
			default:
				return position;
			}
		}
	}
}
