package de.saschahlusiak.freebloks.game;

import de.saschahlusiak.freebloks.Global;
import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.model.GameMode;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

public class ColorListDialog extends Dialog implements OnItemClickListener, OnItemSelectedListener, CompoundButton.OnCheckedChangeListener, View.OnClickListener {
	private OnColorSelectedListener listener;
	private AdapterView<ColorListAdapter> list;
	private ColorListAdapter adapter;
	private Spinner gameMode, fieldSize;
	private CompoundButton passAndPlay;
	private Button button;
	private boolean selection[] = new boolean[4];
	// TODO: names per color in passAndPlay mode

	public interface OnColorSelectedListener {
		void onColorSelected(ColorListDialog dialog, int color);
		void onRandomColorSelected(ColorListDialog dialog);
		void onColorsSelected(ColorListDialog dialog, boolean[] colors);
	}
	
	public ColorListDialog(Context context, final OnColorSelectedListener listener) {
		super(context, R.style.Theme_Freebloks_Light_Dialog);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.color_list_dialog);

		this.listener = listener;
		gameMode = findViewById(R.id.game_mode);
		gameMode.setOnItemSelectedListener(this);
		fieldSize = findViewById(R.id.field_size);
		fieldSize.setOnItemSelectedListener(this);
		
		adapter = new ColorListAdapter(getContext());
		// Can't have the same id for list and grid, otherwise rotate on Android 2.3 crashes
		// with class cast exception
		list = findViewById(android.R.id.list);
		if (list == null)
			list = findViewById(R.id.grid);
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);

		button = findViewById(R.id.startButton);
		button.setOnClickListener(this);
		passAndPlay = findViewById(R.id.pass_and_play);
		passAndPlay.setOnCheckedChangeListener(this);

		adapter.setPassAndPlay(passAndPlay.isChecked());
	}

	@NonNull
	@Override
	public Bundle onSaveInstanceState() {
		Bundle b = super.onSaveInstanceState();
		b.putBooleanArray("color_selection", selection);
		return b;
	}

	@Override
	public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		selection = savedInstanceState.getBooleanArray("color_selection");
		adapter.notifyDataSetChanged();
	}

	void setGameMode(GameMode gamemode) {
	//	setTitle(getContext().getResources().getStringArray(R.array.game_modes)[gamemode.ordinal()]);
		gameMode.setSelection(gamemode.ordinal());

		switch (gamemode) {
		case GAMEMODE_2_COLORS_2_PLAYERS:
			fieldSize.setSelection(2);
			selection[1] = selection[3] = false;
			break;
		case GAMEMODE_DUO:
		case GAMEMODE_JUNIOR:
			fieldSize.setSelection(1);
			selection[1] = selection[3] = false;
			break;
		case GAMEMODE_4_COLORS_2_PLAYERS:
			selection[2] = selection[3] = false;
			break;
		default:
			fieldSize.setSelection(4);
			break;
		}

		adapter.setGameMode(gamemode);
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		if (passAndPlay.isChecked()) {
			selection[(int)id] = !selection[(int)id];
			this.adapter.notifyDataSetChanged();
		} else {
			listener.onColorSelected(this, (int) id);
		}
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
	
	int getBoardSize() {
		return CustomGameDialog.FIELD_SIZES[fieldSize.getSelectedItemPosition()];
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		adapter.setPassAndPlay(passAndPlay.isChecked());
		if (passAndPlay.isChecked()) {
			button.setText(R.string.start);
		} else {
			button.setText(R.string.random_color);
		}
	}

	@Override
	public void onClick(View v) {
		if (passAndPlay.isChecked()) {
			listener.onColorsSelected(this, selection);
		} else {
			listener.onRandomColorSelected(this);
		}
	}

	class ColorListAdapter extends ArrayAdapter<String> {
		private String[] colors;
		private GameMode gamemode;
		private boolean passAndPlay;

		ColorListAdapter(Context context) {
			super(context, R.layout.color_list_item);

			colors = context.getResources().getStringArray(R.array.color_names);
			this.gamemode = null;
		}

		public void setPassAndPlay(boolean passAndPlay) {
			this.passAndPlay = passAndPlay;
			notifyDataSetChanged();
		}

		@Override
		public boolean areAllItemsEnabled() {
			return false;
		}

		@Override
		public boolean isEnabled(int position) {
			if (passAndPlay && gamemode == GameMode.GAMEMODE_4_COLORS_2_PLAYERS) {
				if (getItemId(position) > 1)
					return false;
			}

			return true;
		}

		@NonNull
		@Override
		public View getView(int position, View convertView, @NonNull ViewGroup parent) {
			final View view;
			if (convertView == null) {
				view = LayoutInflater.from(getContext()).inflate(R.layout.color_list_item, parent, false);
			} else {
				view = convertView;
			}

			TextView t = view.findViewById(android.R.id.text1);
			t.setText(getItem(position));

			View color = view.findViewById(R.id.color);

			LayerDrawable ld = (LayerDrawable)getContext().getResources().getDrawable(R.drawable.bg_card_1);
			ld.mutate();
			GradientDrawable drawable;
			
			drawable = ((GradientDrawable)ld.findDrawableByLayerId(R.id.shadow));
			int res = Global.PLAYER_BACKGROUND_COLOR_RESOURCE[Global.getPlayerColor((int)getItemId(position), gamemode)];
			drawable.setColor(getContext().getResources().getColor(res));

			drawable = ((GradientDrawable)ld.findDrawableByLayerId(R.id.color1));
			res = Global.PLAYER_FOREGROUND_COLOR_RESOURCE[Global.getPlayerColor((int)getItemId(position), gamemode)];
			drawable.setColor(getContext().getResources().getColor(res));
//			c.setBackgroundColor(Global.PLAYER_FOREGROUND_COLOR[Global.getPlayerColor((int)getItemId(position), gamemode)]);
			
			color.setBackgroundDrawable(ld);

			if (passAndPlay) {
				CheckBox c = view.findViewById(R.id.checkBox);
				c.setEnabled(true);
				int itemId = (int)getItemId(position);
				if (gamemode == GameMode.GAMEMODE_4_COLORS_2_PLAYERS) {
					if (itemId > 1) {
						itemId -= 2;
						c.setEnabled(false);
					}
				}
				c.setVisibility(View.VISIBLE);
				c.setChecked(selection[itemId]);
			} else {
				view.findViewById(R.id.checkBox).setVisibility(View.GONE);
			}

			return view;
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
