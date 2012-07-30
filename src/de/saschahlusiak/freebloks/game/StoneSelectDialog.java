package de.saschahlusiak.freebloks.game;

import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.model.Player;
import de.saschahlusiak.freebloks.model.Stone;
import de.saschahlusiak.freebloks.view.SimpleStoneView;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;

public class StoneSelectDialog extends Dialog implements OnItemClickListener {
	public interface OnStoneSelectListener {
		public void onClick(DialogInterface dialog, Stone stone);
	}

	OnStoneSelectListener listener;
	GridView grid;
	StoneSelectAdapter adapter;
	Player player;

	class StoneSelectAdapter extends BaseAdapter {
		Player player;
		Context context;

		StoneSelectAdapter(Context context, Player player) {
			this.context = context;
			setPlayer(player);
		}

		void setPlayer(Player player) {
			this.player = player;
		}

		@Override
		public int getCount() {
			if (player == null)
				return 0;
			int c = 0;
			for (int i = 0; i < Stone.STONE_COUNT_ALL_SHAPES; i++)
				if (player.get_stone(i).get_available() > 0)
					c++;
			return c;
		}

		@Override
		public Stone getItem(int position) {
			int i = 0;
			do {
				while (player.get_stone(i).get_available() == 0) {
					i++;
					if (i >= Stone.STONE_COUNT_ALL_SHAPES)
						return null;
				}
				if (position == 0)
					return player.get_stone(i);

				position--;
				i++;
			} while (i < Stone.STONE_COUNT_ALL_SHAPES);
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Stone stone = (Stone) getItem(position);
			View v = new SimpleStoneView(context, player.getPlayerNumber(),
					stone);
			return v;
		}
	}

	public StoneSelectDialog(Context context, OnStoneSelectListener listener,
			Player player) {
		super(context, true, null);
		this.listener = listener;
		setContentView(R.layout.stone_select_layout);

		setTitle("blubb"); /* TODO: translate */

		grid = (GridView) findViewById(R.id.grid);
		grid.setOnItemClickListener(this);
		adapter = new StoneSelectAdapter(getContext(), player);
		grid.setAdapter(adapter);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		listener.onClick(this, adapter.getItem(position));
		dismiss();
	}
}
